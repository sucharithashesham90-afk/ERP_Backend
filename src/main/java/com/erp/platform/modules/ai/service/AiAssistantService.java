package com.erp.platform.modules.ai.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.ai.dto.ChatRequest;
import com.erp.platform.modules.ai.dto.ChatResponse;
import com.erp.platform.modules.ai.service.provider.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Runs the ERP assistant's agent loop: offer permission-filtered tools, execute whatever the model
 * asks for, feed the results back, repeat until it answers.
 *
 * <p>The LLM itself is pluggable — see {@link AiChatProvider} and the {@code ai.provider} property.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAssistantService {

    /** Which backend answers: "gemini" (Google AI Studio free tier) or "claude" (Anthropic). */
    @Value("${ai.provider:gemini}")
    private String providerId;

    /** Upper bound on tool round-trips per question, so a confused model cannot loop forever. */
    @Value("${ai.max-tool-rounds:3}")
    private int maxToolRounds;

    private final List<AiChatProvider> providers;
    private final McpToolExecutor toolExecutor;
    private final TenantContext tenantContext;

    private AiChatProvider provider;

    @PostConstruct
    public void init() {
        provider = providers.stream()
                .filter(p -> p.id().equalsIgnoreCase(providerId))
                .findFirst()
                .orElse(null);

        if (provider == null) {
            // Deliberately not fatal — a typo in ai.provider should degrade the assistant, not block boot.
            log.error("Unknown ai.provider '{}'. Available: {}. AI assistant disabled.",
                    providerId, providers.stream().map(AiChatProvider::id).toList());
        } else {
            log.info("AI assistant provider: {} (configured: {})", provider.id(), provider.isConfigured());
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public ChatResponse chat(ChatRequest req) {
        if (provider == null) {
            return error("AI Assistant is misconfigured: unknown ai.provider '" + providerId + "'.");
        }
        if (!provider.isConfigured()) {
            return error(provider.configHint());
        }

        UUID tenantId = tenantContext.current();
        List<String> allowedModules = getAllowedModules();
        List<AiTool> tools = buildTools(allowedModules);
        String systemPrompt = buildSystemPrompt(allowedModules);

        List<AiMessage> conversation = new ArrayList<>();
        if (req.getHistory() != null) {
            for (Map<String, String> turn : req.getHistory()) {
                String content = turn.get("content");
                if (content == null) continue;
                conversation.add("assistant".equalsIgnoreCase(turn.get("role"))
                        ? AiMessage.assistantText(content)
                        : AiMessage.user(content));
            }
        }
        conversation.add(AiMessage.user(req.getMessage()));

        List<String> toolsUsed = new ArrayList<>();

        try {
            for (int round = 0; round <= maxToolRounds; round++) {
                AiReply reply = provider.send(systemPrompt, conversation, tools);

                if (!reply.wantsTools()) {
                    return new ChatResponse(reply.text(), toolsUsed, allowedModules, true, null);
                }

                if (round == maxToolRounds) {
                    log.warn("AI assistant hit the {}-round tool limit for tenant {}", maxToolRounds, tenantId);
                    String text = reply.text() == null || reply.text().isBlank()
                            ? "I gathered the data but ran out of steps before finishing. Please narrow the question."
                            : reply.text();
                    return new ChatResponse(text, toolsUsed, allowedModules, true, null);
                }

                // Replay the assistant turn in the provider's own shape (preserves Gemini thought signatures).
                conversation.add(AiMessage.assistantRaw(reply.raw()));

                List<AiToolResult> results = new ArrayList<>();
                for (AiToolCall call : reply.toolCalls()) {
                    toolsUsed.add(call.name());
                    log.info("Executing MCP tool: {} for tenant {}", call.name(), tenantId);
                    String result = toolExecutor.execute(call.name(), call.arguments(), tenantId, allowedModules);
                    results.add(new AiToolResult(call.id(), call.name(), result));
                }
                conversation.add(AiMessage.toolResults(results));
            }

            return error("AI request did not converge on an answer.");

        } catch (GeminiChatProvider.AiQuotaExhaustedException e) {
            // Already phrased for the person reading it, and not a fault to log a stack trace for —
            // running out of a free allowance is an expected state, not a failure of the code.
            log.warn("AI provider quota exhausted: {}", e.getMessage());
            return error(e.getMessage());

        } catch (Exception e) {
            log.error("AI chat failed: {}", e.getMessage(), e);
            return error("AI request failed: " + e.getMessage());
        }
    }

    /** Returns the list of module keys the current user is allowed to access. */
    public List<String> getAllowedModules() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return List.of();

        Set<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // ADMIN role → all modules
        if (authorities.contains("ROLE_ADMIN") || authorities.contains("ROLE_admin")) {
            return List.of("SALES", "INVOICES", "INVENTORY", "PURCHASE", "ACCOUNTING",
                           "HR", "PAYROLL", "MANUFACTURING", "CRM", "DASHBOARD");
        }

        // Collect from READ permissions: pattern <MODULE>_READ
        List<String> modules = new ArrayList<>();
        modules.add("DASHBOARD"); // everyone gets dashboard
        for (String auth1 : authorities) {
            if (auth1.endsWith("_READ")) {
                String mod = auth1.substring(0, auth1.length() - 5).toUpperCase();
                modules.add(mod);
            }
            // also handle bare role names like ROLE_SALES, ROLE_HR, ROLE_INVENTORY
            if (auth1.startsWith("ROLE_")) {
                String roleMod = auth1.substring(5).toUpperCase();
                switch (roleMod) {
                    case "SALES"         -> { modules.add("SALES"); modules.add("INVOICES"); }
                    case "PURCHASE"      -> modules.add("PURCHASE");
                    case "INVENTORY"     -> modules.add("INVENTORY");
                    case "ACCOUNTANT"    -> modules.add("ACCOUNTING");
                    case "HR"            -> { modules.add("HR"); modules.add("PAYROLL"); }
                    case "MANUFACTURING" -> modules.add("MANUFACTURING");
                    case "CRM"           -> modules.add("CRM");
                    case "MANAGER"       -> modules.addAll(List.of("SALES","INVOICES","INVENTORY","PURCHASE"));
                }
            }
        }
        return modules.stream().distinct().collect(Collectors.toList());
    }

    // ── Tool Definitions (filtered by allowed modules) ─────────────────────────

    private List<AiTool> buildTools(List<String> allowedModules) {
        List<AiTool> all = new ArrayList<>();

        all.add(tool("get_dashboard_summary",
                "Get a high-level summary of key ERP metrics: customers, invoices, purchase orders, employees.",
                Map.of()));

        if (allowedModules.contains("SALES") || allowedModules.contains("INVOICES")) {
            all.add(tool("get_unpaid_invoices",
                    "Get list of unpaid or partially paid invoices. Optionally filter by minimum amount or customer name.",
                    Map.of("min_amount", prop("number", "Minimum invoice amount in ₹ (default: 0)"),
                           "customer_name", prop("string", "Filter by customer name (partial match)"))));
            all.add(tool("get_sales_orders",
                    "Get sales orders. Optionally filter by status (DRAFT, CONFIRMED, PROCESSING, SHIPPED, DELIVERED).",
                    Map.of("status", prop("string", "Order status to filter by"))));
            all.add(tool("get_customer_list",
                    "Get list of customers. Optionally search by name or email.",
                    Map.of("query", prop("string", "Search query for customer name or email"))));
            all.add(tool("get_sales_summary",
                    "Get overall sales summary: total invoices, billed amount, outstanding amount.", Map.of()));
            all.add(tool("get_customer_advances",
                    "Get active customer advance payments (AVAILABLE or PARTIALLY_APPLIED).", Map.of()));
        }

        if (allowedModules.contains("INVENTORY")) {
            all.add(tool("get_low_stock_items",
                    "Get products with stock quantity at or below the threshold.",
                    Map.of("threshold", prop("number", "Stock quantity threshold (default: 10)"))));
            all.add(tool("get_stock_summary",
                    "Get inventory summary: total SKUs, total quantity on hand, out-of-stock count.", Map.of()));
            all.add(tool("get_expiring_lots",
                    "Get lots expiring within the specified number of days.",
                    Map.of("days", prop("number", "Number of days to look ahead (default: 30)"))));
            all.add(tool("get_quarantined_lots",
                    "Get all lots currently in quarantine status.", Map.of()));
        }

        if (allowedModules.contains("PURCHASE")) {
            all.add(tool("get_pending_purchase_orders",
                    "Get purchase orders in DRAFT, CONFIRMED, or APPROVED status.", Map.of()));
            all.add(tool("get_pending_grn",
                    "Get goods receipt notes pending processing.", Map.of()));
            all.add(tool("get_supplier_list",
                    "Get list of suppliers. Optionally search by name.",
                    Map.of("query", prop("string", "Search query for supplier name"))));
        }

        if (allowedModules.contains("ACCOUNTING")) {
            all.add(tool("get_outstanding_receivables",
                    "Get total outstanding accounts receivable and top customers by balance.", Map.of()));
            all.add(tool("get_outstanding_payables",
                    "Get total outstanding accounts payable from purchase orders.", Map.of()));
            all.add(tool("get_draft_journal_entries",
                    "Get recent draft journal entries pending review and posting.", Map.of()));
        }

        if (allowedModules.contains("HR")) {
            all.add(tool("get_pending_leave_requests",
                    "Get leave applications awaiting approval.", Map.of()));
            all.add(tool("get_employee_summary",
                    "Get active employee count, optionally broken down by department.", Map.of()));
        }

        if (allowedModules.contains("PAYROLL")) {
            all.add(tool("get_payroll_summary",
                    "Get payroll totals for a specific month and year.",
                    Map.of("month", prop("number", "Month number 1-12 (default: current month)"),
                           "year",  prop("number", "Year (default: current year)"))));
            all.add(tool("get_pending_payslips",
                    "Get count of payslips in DRAFT status awaiting approval.", Map.of()));
        }

        if (allowedModules.contains("MANUFACTURING")) {
            all.add(tool("get_active_work_orders",
                    "Get work orders currently PLANNED or IN_PROGRESS.", Map.of()));
            all.add(tool("get_production_summary",
                    "Get count of work orders by status.", Map.of()));
        }

        if (allowedModules.contains("CRM") || allowedModules.contains("LEADS")) {
            all.add(tool("get_leads_pipeline",
                    "Get CRM leads pipeline summary grouped by status with estimated values.", Map.of()));
            all.add(tool("get_won_leads",
                    "Get recently won leads.", Map.of()));
        }

        return all;
    }

    private AiTool tool(String name, String description, Map<String, Object> properties) {
        return new AiTool(name, description, properties);
    }

    private Map<String, Object> prop(String type, String description) {
        return Map.of("type", type, "description", description);
    }

    // ── System Prompt ─────────────────────────────────────────────────────────

    private String buildSystemPrompt(List<String> allowedModules) {
        return """
                You are an intelligent ERP assistant. Your job is to help users understand and query
                their business data using the provided tools.

                Guidelines:
                - Use tools to fetch real data before answering questions about business metrics.
                - Format numbers in Indian currency (₹) with commas for readability.
                - Reply in Markdown. The interface renders it, so use it:
                    * Any list of records goes in a Markdown table with a header row. Do not draw
                      tables with dashes and spaces — they arrive as an unreadable block on narrow
                      screens, which is why this instruction changed.
                    * Put amounts and quantities in their own columns so they line up.
                    * Lead with the answer in one sentence, then the table beneath it.
                    * Use **bold** for the figure the user actually asked for.
                    * Use short headings only when an answer covers several topics.
                - Never widen a table beyond about six columns; drop the least useful ones instead.
                - Be concise and business-focused in responses.
                - If a question is outside the user's permitted modules, politely explain the access restriction.
                - Today's date is: """ + java.time.LocalDate.now() + """

                The user has access to the following ERP modules: """ + String.join(", ", allowedModules) + """

                Only provide information from modules listed above. Do not attempt to access data
                from modules not in the user's allowed list.
                """;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ChatResponse error(String message) {
        return new ChatResponse(message, List.of(), List.of(), false, message);
    }

    /** The provider id this instance resolved, for the status endpoint. */
    public String activeProviderId() {
        return provider != null ? provider.id() : providerId;
    }

    /** Whether that provider has what it needs to run. */
    public boolean isProviderConfigured() {
        return provider != null && provider.isConfigured();
    }
}

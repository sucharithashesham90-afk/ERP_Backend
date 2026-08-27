package com.erp.platform.modules.ai.service.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Anything that speaks the OpenAI chat API.
 *
 * <p>Most providers converged on one wire format, so a single implementation covers a lot of ground.
 * That matters here because Google AI Studio's free tier caps requests per day per model — twenty,
 * on the key this was first wired to — and no code changes a quota someone else enforces. Switching
 * to a provider with more headroom is a config change rather than a new integration.
 *
 * <p>Set {@code ai.provider=openai-compatible} and point it somewhere:
 *
 * <pre>
 *   Groq        base-url https://api.groq.com/openai/v1
 *               model    llama-3.3-70b-versatile        (needs GROQ_API_KEY)
 *
 *   OpenRouter  base-url https://openrouter.ai/api/v1
 *               model    meta-llama/llama-3.3-70b-instruct
 *                        ...or any ":free" model        (needs OPENROUTER_API_KEY)
 *
 *   Ollama      base-url http://localhost:11434         (no key; runs on your machine,
 *               model    qwen2.5:7b                      so there is no quota at all)
 * </pre>
 *
 * <p>The assistant calls tools, so whichever model is chosen has to support tool/function calling.
 * A model without it will answer in prose and never touch the ERP data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiCompatibleChatProvider implements AiChatProvider {

    /** Ollama's default. Point at Groq, OpenRouter, or any other OpenAI-compatible server. */
    @Value("${openai.compat.base-url:http://localhost:11434}")
    private String baseUrl;

    /** Must support tool calling, or the assistant can answer but never read your ERP data. */
    @Value("${openai.compat.model:qwen2.5:7b}")
    private String model;

    /** Required by Groq and OpenRouter; a local Ollama needs none. */
    @Value("${openai.compat.api-key:}")
    private String apiKey;

    @Value("${openai.compat.max-tokens:4096}")
    private int maxTokens;

    private final ObjectMapper objectMapper;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        baseUrl = normalizeBaseUrl(baseUrl);
        restClient = RestClient.builder().baseUrl(baseUrl).build();
        // Mirrors the Gemini diagnostic: separates "the platform never passed the variable" from
        // "the property wiring is broken". Logs only whether values exist — never the key itself.
        log.info("OpenAI-compatible provider ready: baseUrl='{}', model={}, api-key resolved={}", baseUrl, model, StringUtils.hasText(apiKey));
        log.info("  env OPENAI_COMPAT_BASE_URL present={}, OPENAI_COMPAT_API_KEY present={}, AI_PROVIDER env='{}'",
                System.getenv("OPENAI_COMPAT_BASE_URL") != null,
                System.getenv("OPENAI_COMPAT_API_KEY") != null,
                System.getenv("AI_PROVIDER"));
    }

    @Override
    public String id() {
        return "openai-compatible";
    }

    /**
     * Accept the base URL in either form, because requests append {@code /v1/chat/completions}.
     *
     * <p>Every provider documents its endpoint <em>with</em> the version segment — Groq publishes
     * {@code https://api.groq.com/openai/v1}, OpenRouter {@code https://openrouter.ai/api/v1} — so
     * pasting the documented value is the natural thing to do, and it produced a doubled
     * {@code /v1/v1/chat/completions} and a 404. Trailing slashes and a trailing {@code /v1} are
     * stripped so both the documented URL and the bare host work.
     */
    static String normalizeBaseUrl(String url) {
        if (url == null) return "";
        String s = url.trim();
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        if (s.endsWith("/v1")) s = s.substring(0, s.length() - 3);
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    @Override
    public boolean isConfigured() {
        // A local server needs no credential, so somewhere to talk to is the whole condition.
        // Groq and OpenRouter reject an anonymous call, but that is their error to report.
        return StringUtils.hasText(baseUrl) && StringUtils.hasText(model);
    }

    @Override
    public String configHint() {
        return "No OpenAI-compatible endpoint is configured. Set ai.provider=openai-compatible and "
                + "openai.compat.base-url — Groq (https://api.groq.com/openai/v1), OpenRouter "
                + "(https://openrouter.ai/api/v1), or a local Ollama (http://localhost:11434).";
    }

    @Override
    @SuppressWarnings("unchecked")
    public AiReply send(String systemPrompt, List<AiMessage> conversation, List<AiTool> tools) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(toMessages(conversation));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("max_tokens", maxTokens);
        body.put("temperature", 0.2);
        if (!tools.isEmpty()) {
            body.put("tools", tools.stream().map(this::toToolSpec).toList());
        }

        var request = restClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(apiKey)) {
            request = request.header("Authorization", "Bearer " + apiKey);
        }

        String responseStr = request.body(body).retrieve().body(String.class);

        Map<String, Object> parsed;
        try {
            parsed = objectMapper.readValue(responseStr, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI-compatible response: " + e.getMessage(), e);
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) parsed.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("OpenAI-compatible endpoint returned no choices: " + truncate(responseStr));
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String text = message.get("content") instanceof String s ? s : "";

        List<AiToolCall> calls = new ArrayList<>();
        if (message.get("tool_calls") instanceof List<?> rawCalls) {
            for (Object raw : rawCalls) {
                Map<String, Object> call = (Map<String, Object>) raw;
                Map<String, Object> fn = (Map<String, Object>) call.get("function");
                if (fn == null) continue;
                Map<String, Object> args = new HashMap<>();
                // Arguments arrive as a JSON string here, unlike Gemini which sends an object.
                if (fn.get("arguments") instanceof String argStr && !argStr.isBlank()) {
                    try {
                        args = objectMapper.readValue(argStr, new TypeReference<>() {});
                    } catch (Exception e) {
                        log.warn("Provider sent unparseable tool arguments for {}: {}", fn.get("name"), argStr);
                    }
                }
                calls.add(new AiToolCall((String) call.get("id"), (String) fn.get("name"), args));
            }
        }

        return new AiReply(text, calls, message);
    }

    // ── Wire format translation ───────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toMessages(List<AiMessage> conversation) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (AiMessage msg : conversation) {
            switch (msg.kind()) {
                case USER_TEXT -> out.add(Map.of("role", "user", "content", msg.text()));

                case ASSISTANT_TEXT -> out.add(Map.of("role", "assistant", "content", msg.text()));

                // Handed back as received, so any tool_calls on it survive the round trip.
                case ASSISTANT_RAW -> out.add((Map<String, Object>) msg.providerRaw());

                case TOOL_RESULTS -> {
                    for (AiToolResult result : msg.toolResults()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("role", "tool");
                        m.put("content", result.content());
                        // The id ties the result back to the call that asked for it.
                        if (result.id() != null) m.put("tool_call_id", result.id());
                        m.put("name", result.name());
                        out.add(m);
                    }
                }
            }
        }
        return out;
    }

    private Map<String, Object> toToolSpec(AiTool tool) {
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", tool.name());
        function.put("description", tool.description());
        // A zero-argument tool must omit "parameters" entirely. Sending an object schema with empty
        // properties makes Groq refuse the whole call — "Failed to call a function" — rather than
        // just ignoring it, so every tool that takes no arguments would silently never run. Gemini
        // rejects the same shape; this is the one place the two agree.
        if (tool.properties() != null && !tool.properties().isEmpty()) {
            function.put("parameters", Map.of(
                    "type", "object",
                    "properties", tool.properties(),
                    "required", List.of()));
        }
        return Map.of("type", "function", "function", function);
    }

    private String truncate(String s) {
        return s == null ? "" : s.substring(0, Math.min(s.length(), 400));
    }
}

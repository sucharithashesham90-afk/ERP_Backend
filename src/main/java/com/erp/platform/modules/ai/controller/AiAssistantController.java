package com.erp.platform.modules.ai.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.ai.dto.ChatRequest;
import com.erp.platform.modules.ai.dto.ChatResponse;
import com.erp.platform.modules.ai.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Natural language ERP queries via the configured AI provider")
public class AiAssistantController {

    private final AiAssistantService service;

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a natural language query to the ERP AI assistant")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.chat(request)));
    }

    /**
     * Which provider this running instance is actually using, and whether its variables arrived.
     *
     * <p>Added because three deploys in a row reported Gemini while the Groq variables were said to
     * be set. A log file only proves what some service printed at some point; this answers the same
     * question about the instance actually serving traffic, which is the one that matters when more
     * than one service or environment is in play.
     *
     * <p>Reports presence, never values — a key must not be readable through an API.
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Which AI provider this instance is using, and whether its config arrived")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> status() {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("activeProvider", service.activeProviderId());
        m.put("configured", service.isProviderConfigured());
        java.util.Map<String, Object> env = new java.util.LinkedHashMap<>();
        env.put("AI_PROVIDER", System.getenv("AI_PROVIDER"));
        env.put("OPENAI_COMPAT_BASE_URL", System.getenv("OPENAI_COMPAT_BASE_URL"));
        env.put("OPENAI_COMPAT_MODEL", System.getenv("OPENAI_COMPAT_MODEL"));
        env.put("OPENAI_COMPAT_API_KEY_present", System.getenv("OPENAI_COMPAT_API_KEY") != null);
        env.put("GEMINI_API_KEY_present", System.getenv("GEMINI_API_KEY") != null);
        m.put("environment", env);

        // Every variable name the process can actually see, so a name that looks right in the
        // dashboard but arrives with a stray space, a zero-width character, or not at all stops
        // being a matter of opinion. Names only — never values.
        java.util.List<String> visible = new java.util.ArrayList<>(System.getenv().keySet());
        java.util.Collections.sort(visible);
        m.put("allEnvNamesVisibleToProcess", visible);
        m.put("envCount", visible.size());

        return ResponseEntity.ok(ApiResponse.success(m));
    }

    @GetMapping("/modules")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the list of ERP modules accessible to the current user")
    public ResponseEntity<ApiResponse<List<String>>> allowedModules() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllowedModules()));
    }
}

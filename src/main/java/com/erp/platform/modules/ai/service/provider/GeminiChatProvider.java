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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Google AI Studio (Gemini) backend, talking to the v1beta {@code generateContent} REST endpoint.
 *
 * <p>The free tier covers the Flash family only — Pro models are billed.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiChatProvider implements AiChatProvider {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com";

    @Value("${gemini.api.key:}")
    private String apiKey;

    /** Free-tier options: gemini-3.6-flash, gemini-3.5-flash, gemini-3.5-flash-lite. */
    @Value("${gemini.model:gemini-3.6-flash}")
    private String model;

    /** Flash models spend output tokens on reasoning, so this needs headroom above the visible answer. */
    @Value("${gemini.max-output-tokens:8192}")
    private int maxOutputTokens;

    /**
     * Models to fall back through when the configured one is out of quota.
     *
     * <p>The free-tier allowance is granted <em>per project per model</em> — Google's own quota id
     * says so: {@code GenerateRequestsPerDayPerProjectPerModel-FreeTier}, twenty requests a day.
     * Exhausting one Flash model therefore says nothing about the next, so stepping down the list
     * buys a fresh allowance instead of failing the request.
     *
     * <p>Strongest first; lite last, because a thinner answer beats an error page.
     */
    @Value("${gemini.fallback-models:gemini-3.5-flash,gemini-3.5-flash-lite}")
    private String fallbackModels;

    private final ObjectMapper objectMapper;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        restClient = RestClient.builder().baseUrl(BASE_URL).build();

        // Separates "the platform never passed the variable" from "the property wiring is broken".
        // Logs only whether a value exists — never the value itself.
        log.info("Gemini provider ready: model={}, gemini.api.key resolved={}, GEMINI_API_KEY env present={}",
                model, StringUtils.hasText(apiKey), System.getenv("GEMINI_API_KEY") != null);
    }

    @Override
    public String id() {
        return "gemini";
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    @Override
    public String configHint() {
        return "AI Assistant is not configured. Set the GEMINI_API_KEY environment variable "
                + "(get a free key at https://aistudio.google.com/apikey).";
    }

    @Override
    @SuppressWarnings("unchecked")
    public AiReply send(String systemPrompt, List<AiMessage> conversation, List<AiTool> tools) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
        body.put("contents", toContents(conversation));
        if (!tools.isEmpty()) {
            body.put("tools", List.of(Map.of("functionDeclarations", tools.stream().map(this::toDeclaration).toList())));
        }
        body.put("generationConfig", Map.of(
                "maxOutputTokens", maxOutputTokens,
                "temperature", 0.2
        ));

        String responseStr = callWithFallback(body);

        Map<String, Object> parsed;
        try {
            parsed = objectMapper.readValue(responseStr, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Gemini response: " + e.getMessage(), e);
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) parsed.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("Gemini returned no candidates: " + truncate(responseStr));
        }

        Map<String, Object> candidate = candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
        List<Map<String, Object>> parts = content == null
                ? List.of()
                : (List<Map<String, Object>>) content.getOrDefault("parts", List.of());

        if (parts.isEmpty()) {
            // Usually MAX_TOKENS: reasoning consumed the whole budget before any answer was emitted.
            throw new IllegalStateException("Gemini returned no content (finishReason: "
                    + candidate.get("finishReason") + "). Try raising gemini.max-output-tokens.");
        }

        StringBuilder text = new StringBuilder();
        List<AiToolCall> calls = new ArrayList<>();
        for (Map<String, Object> part : parts) {
            if (part.get("text") instanceof String s) {
                text.append(s);
            }
            if (part.get("functionCall") instanceof Map<?, ?> fc) {
                Map<String, Object> call = (Map<String, Object>) fc;
                Object args = call.get("args");
                calls.add(new AiToolCall(
                        (String) call.get("id"),
                        (String) call.get("name"),
                        args instanceof Map ? (Map<String, Object>) args : new HashMap<>()));
            }
        }

        // parts is handed back untouched so thoughtSignature survives the round trip.
        return new AiReply(text.toString(), calls, parts);
    }

    /**
     * Ask each model in turn until one has quota left.
     *
     * <p>A 429 from Gemini used to travel all the way to the screen as raw Google JSON — the user
     * saw a wall of quota metrics rather than being told the daily allowance had run out. Worse, it
     * gave up on the first model when two more were sitting there with their own untouched
     * allowances.
     */
    private String callWithFallback(Map<String, Object> body) {
        List<String> chain = new ArrayList<>();
        chain.add(model);
        for (String m : fallbackModels.split(",")) {
            String trimmed = m.trim();
            if (!trimmed.isEmpty() && !trimmed.equals(model)) chain.add(trimmed);
        }

        HttpClientErrorException lastQuotaError = null;
        for (String candidateModel : chain) {
            try {
                return restClient.post()
                        .uri("/v1beta/models/{model}:generateContent", candidateModel)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                lastQuotaError = e;
                log.warn("Gemini model {} is out of quota; trying the next one", candidateModel);
            }
        }

        throw new AiQuotaExhaustedException(quotaMessage(lastQuotaError, chain));
    }

    /**
     * What to tell someone whose free allowance has run out.
     *
     * <p>Says which limit was hit and what to do about it. The per-day and per-minute cases need
     * different advice — waiting helps with one and not the other — and Google returns a short
     * retryDelay even for the daily cap, which is actively misleading.
     */
    private String quotaMessage(HttpClientErrorException error, List<String> tried) {
        String raw = error == null ? "" : error.getResponseBodyAsString();
        boolean perDay = raw.contains("PerDay");
        String limit = find(raw, "\"quotaValue\":\s*\"(\\d+)\"");
        String retry = find(raw, "retry in ([\\d.]+)s");

        StringBuilder sb = new StringBuilder();
        if (perDay) {
            sb.append("The AI Assistant has used its free Google AI allowance for today");
            if (limit != null) sb.append(" (").append(limit).append(" requests per model per day)");
            sb.append(". It resets at midnight Pacific time.");
        } else {
            sb.append("The AI Assistant is being rate limited by Google");
            if (retry != null) sb.append("; it should work again in about ").append(retry).append(" seconds");
            sb.append(".");
        }
        sb.append(" Models tried: ").append(String.join(", ", tried)).append(".");
        if (perDay) {
            sb.append(" To carry on now, either add billing to the Google AI project or set");
            sb.append(" ai.provider to another configured provider.");
        }
        return sb.toString();
    }

    private static String find(String haystack, String regex) {
        var m = java.util.regex.Pattern.compile(regex).matcher(haystack == null ? "" : haystack);
        return m.find() ? m.group(1) : null;
    }

    /** Raised when every model in the chain is out of quota, so callers can say so plainly. */
    public static class AiQuotaExhaustedException extends RuntimeException {
        public AiQuotaExhaustedException(String message) {
            super(message);
        }
    }

    // ── Wire format translation ───────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toContents(List<AiMessage> conversation) {
        List<Map<String, Object>> contents = new ArrayList<>();
        for (AiMessage msg : conversation) {
            switch (msg.kind()) {
                case USER_TEXT -> contents.add(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", msg.text()))));

                // Gemini calls the assistant role "model".
                case ASSISTANT_TEXT -> contents.add(Map.of(
                        "role", "model",
                        "parts", List.of(Map.of("text", msg.text()))));

                case ASSISTANT_RAW -> contents.add(Map.of(
                        "role", "model",
                        "parts", (List<Map<String, Object>>) msg.providerRaw()));

                case TOOL_RESULTS -> {
                    List<Map<String, Object>> parts = new ArrayList<>();
                    for (AiToolResult result : msg.toolResults()) {
                        Map<String, Object> fn = new LinkedHashMap<>();
                        if (result.id() != null) fn.put("id", result.id());
                        fn.put("name", result.name());
                        // "response" must be a JSON object, so the tool's string output is wrapped.
                        fn.put("response", Map.of("result", result.content()));
                        parts.add(Map.of("functionResponse", fn));
                    }
                    contents.add(Map.of("role", "user", "parts", parts));
                }
            }
        }
        return contents;
    }

    private Map<String, Object> toDeclaration(AiTool tool) {
        Map<String, Object> declaration = new LinkedHashMap<>();
        declaration.put("name", tool.name());
        declaration.put("description", tool.description());
        // A zero-argument tool must omit "parameters" entirely — an empty OBJECT schema is rejected.
        if (tool.properties() != null && !tool.properties().isEmpty()) {
            declaration.put("parameters", Map.of(
                    "type", "OBJECT",
                    "properties", upperCaseTypes(tool.properties())));
        }
        return declaration;
    }

    /** Gemini's schema dialect expects STRING/NUMBER/BOOLEAN rather than the JSON-Schema lowercase forms. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> upperCaseTypes(Map<String, Object> properties) {
        Map<String, Object> out = new LinkedHashMap<>();
        properties.forEach((key, value) -> {
            if (value instanceof Map<?, ?> raw) {
                Map<String, Object> prop = new LinkedHashMap<>((Map<String, Object>) raw);
                if (prop.get("type") instanceof String type) {
                    prop.put("type", type.toUpperCase(Locale.ROOT));
                }
                out.put(key, prop);
            } else {
                out.put(key, value);
            }
        });
        return out;
    }

    private String truncate(String s) {
        return s == null ? "" : s.substring(0, Math.min(s.length(), 400));
    }
}

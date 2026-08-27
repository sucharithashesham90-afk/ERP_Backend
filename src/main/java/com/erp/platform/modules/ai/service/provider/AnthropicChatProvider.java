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

/** Anthropic Messages API backend. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnthropicChatProvider implements AiChatProvider {

    private static final String BASE_URL = "https://api.anthropic.com";
    private static final String API_VERSION = "2023-06-01";

    @Value("${anthropic.api.key:}")
    private String apiKey;

    @Value("${anthropic.model:claude-sonnet-4-6}")
    private String model;

    @Value("${anthropic.max-tokens:2048}")
    private int maxTokens;

    private final ObjectMapper objectMapper;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    @Override
    public String id() {
        return "claude";
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    @Override
    public String configHint() {
        return "AI Assistant is not configured. Please set the ANTHROPIC_API_KEY environment variable.";
    }

    @Override
    @SuppressWarnings("unchecked")
    public AiReply send(String systemPrompt, List<AiMessage> conversation, List<AiTool> tools) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("system", systemPrompt);
        if (!tools.isEmpty()) {
            body.put("tools", tools.stream().map(this::toToolSpec).toList());
        }
        body.put("messages", toMessages(conversation));

        String responseStr = restClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", API_VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        Map<String, Object> parsed;
        try {
            parsed = objectMapper.readValue(responseStr, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Claude response: " + e.getMessage(), e);
        }

        List<Map<String, Object>> content = (List<Map<String, Object>>) parsed.get("content");
        if (content == null) {
            throw new IllegalStateException("Claude returned no content: " + truncate(responseStr));
        }

        StringBuilder text = new StringBuilder();
        List<AiToolCall> calls = new ArrayList<>();
        for (Map<String, Object> block : content) {
            if ("text".equals(block.get("type")) && block.get("text") instanceof String s) {
                if (!text.isEmpty()) text.append("\n");
                text.append(s);
            }
            if ("tool_use".equals(block.get("type"))) {
                Object input = block.get("input");
                calls.add(new AiToolCall(
                        (String) block.get("id"),
                        (String) block.get("name"),
                        input instanceof Map ? (Map<String, Object>) input : new HashMap<>()));
            }
        }

        return new AiReply(text.toString(), calls, content);
    }

    // ── Wire format translation ───────────────────────────────────────────────

    private List<Map<String, Object>> toMessages(List<AiMessage> conversation) {
        List<Map<String, Object>> messages = new ArrayList<>();
        for (AiMessage msg : conversation) {
            switch (msg.kind()) {
                case USER_TEXT      -> messages.add(Map.of("role", "user", "content", msg.text()));
                case ASSISTANT_TEXT -> messages.add(Map.of("role", "assistant", "content", msg.text()));
                case ASSISTANT_RAW  -> messages.add(Map.of("role", "assistant", "content", msg.providerRaw()));
                case TOOL_RESULTS   -> {
                    List<Map<String, Object>> blocks = new ArrayList<>();
                    for (AiToolResult result : msg.toolResults()) {
                        blocks.add(Map.of(
                                "type", "tool_result",
                                "tool_use_id", result.id(),
                                "content", result.content()));
                    }
                    // Tool output goes back to Claude on a user turn.
                    messages.add(Map.of("role", "user", "content", blocks));
                }
            }
        }
        return messages;
    }

    private Map<String, Object> toToolSpec(AiTool tool) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", tool.properties() == null ? Map.of() : tool.properties());
        return Map.of(
                "name", tool.name(),
                "description", tool.description(),
                "input_schema", schema);
    }

    private String truncate(String s) {
        return s == null ? "" : s.substring(0, Math.min(s.length(), 400));
    }
}

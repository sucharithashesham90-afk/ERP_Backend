package com.erp.platform.modules.ai.service.provider;

import java.util.List;

/**
 * A pluggable LLM backend for the ERP assistant.
 *
 * <p>Implementations translate the provider-neutral conversation model
 * ({@link AiMessage}, {@link AiTool}, {@link AiToolCall}) to and from their own wire format,
 * so {@code AiAssistantService} owns the agent loop and never mentions a vendor.
 */
public interface AiChatProvider {

    /** Value matched against the {@code ai.provider} property, e.g. "gemini" or "claude". */
    String id();

    /** False when the API key for this provider is missing. */
    boolean isConfigured();

    /** Shown to the user when {@link #isConfigured()} is false. */
    String configHint();

    /**
     * One request/response round. Never executes tools — it only reports the calls
     * the model asked for, which the caller runs and feeds back as a
     * {@link AiMessage.Kind#TOOL_RESULTS} message.
     */
    AiReply send(String systemPrompt, List<AiMessage> conversation, List<AiTool> tools);
}

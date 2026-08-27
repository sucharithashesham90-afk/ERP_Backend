package com.erp.platform.modules.ai.service.provider;

import java.util.List;

/**
 * One provider-neutral turn in the conversation.
 *
 * <p>{@link Kind#ASSISTANT_RAW} carries the provider's own untouched response payload so it can be
 * replayed verbatim on the next request. This matters: Gemini 3 attaches a {@code thoughtSignature}
 * to each function call and rejects the follow-up if it is not echoed back, and rebuilding the turn
 * from parsed fields would drop it.
 */
public record AiMessage(Kind kind, String text, Object providerRaw, List<AiToolResult> toolResults) {

    public enum Kind {
        /** Something the user typed. */
        USER_TEXT,
        /** A plain-text assistant turn replayed from client-supplied history. */
        ASSISTANT_TEXT,
        /** An assistant turn from this session, stored in the provider's native shape. */
        ASSISTANT_RAW,
        /** Output of the tools the assistant asked for, returned to the model. */
        TOOL_RESULTS
    }

    public static AiMessage user(String text) {
        return new AiMessage(Kind.USER_TEXT, text, null, List.of());
    }

    public static AiMessage assistantText(String text) {
        return new AiMessage(Kind.ASSISTANT_TEXT, text, null, List.of());
    }

    public static AiMessage assistantRaw(Object providerRaw) {
        return new AiMessage(Kind.ASSISTANT_RAW, null, providerRaw, List.of());
    }

    public static AiMessage toolResults(List<AiToolResult> results) {
        return new AiMessage(Kind.TOOL_RESULTS, null, null, results);
    }
}

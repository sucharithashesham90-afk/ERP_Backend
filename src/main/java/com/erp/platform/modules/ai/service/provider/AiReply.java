package com.erp.platform.modules.ai.service.provider;

import java.util.List;

/**
 * One model response.
 *
 * @param text      any prose the model produced (may be blank when it only asked for tools)
 * @param toolCalls tools the model wants executed; empty means the answer is final
 * @param raw       the provider's native turn payload, replayed via {@link AiMessage#assistantRaw}
 */
public record AiReply(String text, List<AiToolCall> toolCalls, Object raw) {

    public boolean wantsTools() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}

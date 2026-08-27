package com.erp.platform.modules.ai.service.provider;

import java.util.Map;

/** A tool invocation requested by the model. {@code id} correlates it with its result. */
public record AiToolCall(String id, String name, Map<String, Object> arguments) {
}

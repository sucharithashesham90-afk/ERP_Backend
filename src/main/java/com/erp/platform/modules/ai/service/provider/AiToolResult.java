package com.erp.platform.modules.ai.service.provider;

/** The output of one executed tool, sent back to the model. */
public record AiToolResult(String id, String name, String content) {
}

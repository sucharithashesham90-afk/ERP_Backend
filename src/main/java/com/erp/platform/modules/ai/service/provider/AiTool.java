package com.erp.platform.modules.ai.service.provider;

import java.util.Map;

/**
 * A tool offered to the model.
 *
 * @param properties JSON-Schema property map; empty means the tool takes no arguments.
 */
public record AiTool(String name, String description, Map<String, Object> properties) {
}

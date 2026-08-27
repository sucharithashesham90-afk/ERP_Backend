package com.erp.platform.modules.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String response;
    private List<String> toolsUsed;
    private List<String> allowedModules;
    private boolean success;
    private String error;
}

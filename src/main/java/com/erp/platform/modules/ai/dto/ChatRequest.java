package com.erp.platform.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    @NotBlank
    private String message;
    /** Optional prior turns: each entry is {"role":"user"|"assistant", "content":"..."} */
    private List<Map<String, String>> history;
}

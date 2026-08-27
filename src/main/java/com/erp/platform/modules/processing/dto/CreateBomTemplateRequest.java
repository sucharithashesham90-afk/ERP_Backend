package com.erp.platform.modules.processing.dto;

public record CreateBomTemplateRequest(
        String bomCode,
        String bomName,
        String outputProductName,
        String outputUnit,
        String bomCategory,
        String processType,
        String description,
        boolean active
) {}

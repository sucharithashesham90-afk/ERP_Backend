package com.erp.platform.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateDepartmentRequest {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Department name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 30)
    private String code;

    @Size(max = 500)
    private String description;

    private UUID parentId;
}

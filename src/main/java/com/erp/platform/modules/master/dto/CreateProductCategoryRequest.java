package com.erp.platform.modules.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateProductCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 30)
    private String code;

    @Size(max = 500)
    private String description;

    private UUID parentId;
}

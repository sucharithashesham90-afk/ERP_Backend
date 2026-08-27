package com.erp.platform.modules.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateBranchRequest {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Branch name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 30)
    private String code;

    @Size(max = 500)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String phone;

    @Size(max = 100)
    private String email;

    private boolean isHeadOffice = false;
}

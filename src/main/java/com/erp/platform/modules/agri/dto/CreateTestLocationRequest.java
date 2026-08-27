package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTestLocationRequest {

    @NotBlank
    private String name;

    private String description;
    private String city;
    private String state;
    private boolean active = true;
}

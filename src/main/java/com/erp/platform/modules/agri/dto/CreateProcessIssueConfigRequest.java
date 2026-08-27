package com.erp.platform.modules.agri.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProcessIssueConfigRequest {

    @NotBlank
    private String processType;

    private String byproductName;

    // Lombok makes the getter isEnabled()/setter setEnabled(), which Jackson maps to "enabled";
    // pin the JSON name so the frontend's isEnabled binds correctly.
    @JsonProperty("isEnabled")
    private boolean isEnabled = true;

    private String issueCategory;
}

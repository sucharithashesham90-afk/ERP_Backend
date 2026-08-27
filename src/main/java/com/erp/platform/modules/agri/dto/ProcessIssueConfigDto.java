package com.erp.platform.modules.agri.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProcessIssueConfigDto {

    private UUID id;
    private String processType;
    private String byproductName;
    @JsonProperty("isEnabled")
    private boolean isEnabled;
    private String issueCategory;
    private LocalDateTime createdAt;
}

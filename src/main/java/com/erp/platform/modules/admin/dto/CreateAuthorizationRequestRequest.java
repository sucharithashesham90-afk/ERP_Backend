package com.erp.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateAuthorizationRequestRequest {

    @NotBlank
    private String requestedBy;

    private String requestedFor;
    private String module;
    private String action;
    private String justification;
    private String status;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewComments;
}

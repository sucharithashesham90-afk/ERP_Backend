package com.erp.platform.modules.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AuthorizationRequestDto {

    private UUID id;
    private String requestedBy;
    private String requestedFor;
    private String module;
    private String action;
    private String justification;
    private String status;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewComments;
    private LocalDateTime createdAt;
}

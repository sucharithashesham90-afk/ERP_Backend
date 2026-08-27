package com.erp.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateLoginTrackingRequest {

    @NotBlank
    private String userId;

    private String username;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String ipAddress;
    private Long sessionDurationMinutes;
    private String status;
}

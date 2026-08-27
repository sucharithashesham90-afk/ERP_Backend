package com.erp.platform.modules.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class LoginTrackingDto {

    private UUID id;
    private String userId;
    private String username;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String ipAddress;
    private Long sessionDurationMinutes;
    private String status;
    private boolean forceLoggedOff;
    private String forceLogoffBy;
    private LocalDateTime forceLogoffAt;
    private LocalDateTime createdAt;
}

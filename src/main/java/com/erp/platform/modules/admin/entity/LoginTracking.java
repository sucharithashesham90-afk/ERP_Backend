package com.erp.platform.modules.admin.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_tracking", indexes = {@Index(name = "idx_login_tracking_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class LoginTracking extends TenantEntity {

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "username", length = 200)
    private String username;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "session_duration_minutes")
    private Long sessionDurationMinutes;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "force_logged_off")
    private boolean forceLoggedOff = false;

    @Column(name = "force_logoff_by", length = 150)
    private String forceLogoffBy;

    @Column(name = "force_logoff_at")
    private LocalDateTime forceLogoffAt;
}

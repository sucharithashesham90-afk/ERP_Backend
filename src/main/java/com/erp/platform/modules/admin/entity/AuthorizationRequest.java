package com.erp.platform.modules.admin.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "authorization_requests", indexes = {@Index(name = "idx_authorization_request_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class AuthorizationRequest extends TenantEntity {

    @Column(name = "requested_by", length = 150)
    private String requestedBy;

    @Column(name = "requested_for", length = 150)
    private String requestedFor;

    @Column(name = "module", length = 100)
    private String module;

    @Column(name = "action", length = 200)
    private String action;

    @Column(name = "justification", length = 1000)
    private String justification;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "reviewed_by", length = 150)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_comments", length = 500)
    private String reviewComments;
}

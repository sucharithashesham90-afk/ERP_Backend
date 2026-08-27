package com.erp.platform.modules.crm.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leads",
       indexes = {
           @Index(name = "idx_lead_tenant", columnList = "tenant_id"),
           @Index(name = "idx_lead_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class Lead extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String company;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private LeadSource source = LeadSource.WEBSITE;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private LeadStatus status = LeadStatus.NEW;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private LeadPriority priority = LeadPriority.MEDIUM;

    @Column(name = "estimated_value", precision = 18, scale = 2)
    private BigDecimal estimatedValue = BigDecimal.ZERO;

    @Column(length = 50)
    private String industry;

    @Column(length = 1000)
    private String notes;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    public enum LeadSource {
        WEBSITE, REFERRAL, COLD_CALL, SOCIAL_MEDIA, EMAIL, OTHER
    }

    public enum LeadStatus {
        NEW, CONTACTED, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST
    }

    public enum LeadPriority {
        LOW, MEDIUM, HIGH
    }
}

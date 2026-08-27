package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tada_claims", indexes = {@Index(name = "idx_tada_claim_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class TadaClaim extends TenantEntity {

    @Column(name = "employee_id", length = 100)
    private String employeeId;

    @Column(name = "employee_name", length = 200)
    private String employeeName;

    @Column(name = "claim_date")
    private LocalDate claimDate;

    @Column(name = "travel_type", length = 100)
    private String travelType;

    @Column(name = "from_place", length = 200)
    private String fromPlace;

    @Column(name = "to_place", length = 200)
    private String toPlace;

    @Column(name = "travel_days")
    private Integer travelDays;

    @Column(name = "daily_allowance_rate", precision = 15, scale = 2)
    private BigDecimal dailyAllowanceRate;

    @Column(name = "travel_allowance", precision = 15, scale = 2)
    private BigDecimal travelAllowance;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "remarks", length = 500)
    private String remarks;
}

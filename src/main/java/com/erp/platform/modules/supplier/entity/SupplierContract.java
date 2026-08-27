package com.erp.platform.modules.supplier.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "supplier_contracts",
        indexes = {
                @Index(name = "idx_sc_tenant", columnList = "tenant_id"),
                @Index(name = "idx_sc_vendor", columnList = "tenant_id, vendor_id"),
                @Index(name = "idx_sc_status", columnList = "tenant_id, status")
        })
@Getter
@Setter
public class SupplierContract extends TenantEntity {

    @Column(name = "contract_number", nullable = false, length = 50)
    private String contractNumber;

    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 30)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "total_contract_value", precision = 18, scale = 2)
    private BigDecimal totalContractValue;

    @Column(length = 10)
    private String currency = "INR";

    @Column(name = "payment_terms", length = 500)
    private String paymentTerms;

    @Column(name = "delivery_terms", length = 500)
    private String deliveryTerms;

    @Column(name = "quality_terms", length = 1000)
    private String qualityTerms;

    @Column(name = "penalty_clause", length = 1000)
    private String penaltyClause;

    @Column(name = "notice_period_days")
    private int noticePeriodDays = 30;

    @Column(name = "auto_renew")
    private boolean autoRenew = false;

    @Column(name = "renewal_term_months")
    private int renewalTermMonths = 12;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractItem> items = new ArrayList<>();

    public enum ContractType {
        ANNUAL, SPOT, FRAMEWORK, SERVICE_LEVEL_AGREEMENT
    }

    public enum ContractStatus {
        DRAFT, ACTIVE, EXPIRING_SOON, EXPIRED, TERMINATED
    }
}

package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriProcessIssuance")
@Table(name = "agri_process_issuances", indexes = {@Index(name = "idx_pi_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProcessIssuance extends TenantEntity {

    @Column(name = "process_number", length = 50, nullable = false)
    private String processNumber;

    @Column(name = "process_date")
    private LocalDate processDate;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "process_type", length = 50)
    private String processType;

    @Column(name = "by_product_name", length = 100)
    private String byProductName;

    @Column(name = "forward_weight_kgs", precision = 15, scale = 3)
    private BigDecimal forwardWeightKgs;

    @Column(name = "expired_kgs", precision = 15, scale = 3)
    private BigDecimal expiredKgs;

    @Column(name = "lost_kgs", precision = 15, scale = 3)
    private BigDecimal lostKgs;

    @Column(name = "rejected_kgs", precision = 15, scale = 3)
    private BigDecimal rejectedKgs;

    @Column(name = "retained_kgs", precision = 15, scale = 3)
    private BigDecimal retainedKgs;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "status", length = 20)
    private String status = "PENDING";

    @Column(name = "remarks", length = 500)
    private String remarks;
}

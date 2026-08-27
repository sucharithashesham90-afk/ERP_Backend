package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriDealerClaim")
@Table(name = "agri_dealer_claims", indexes = {@Index(name = "idx_dc_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class DealerClaim extends TenantEntity {

    @Column(name = "claim_number", length = 50, nullable = false)
    private String claimNumber;

    @Column(name = "claim_date")
    private LocalDate claimDate;

    @Column(name = "dealer_name", length = 200)
    private String dealerName;

    @Column(name = "scheme_name", length = 200)
    private String schemeName;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "quantity_sold", precision = 15, scale = 3)
    private BigDecimal quantitySold;

    @Column(name = "claim_amount_per_unit", precision = 15, scale = 4)
    private BigDecimal claimAmountPerUnit;

    @Column(name = "total_claim", precision = 15, scale = 2)
    private BigDecimal totalClaim;

    @Column(name = "supporting_docs_ref", length = 200)
    private String supportingDocsRef;

    @Column(name = "status", length = 20)
    private String status = "PENDING";
}

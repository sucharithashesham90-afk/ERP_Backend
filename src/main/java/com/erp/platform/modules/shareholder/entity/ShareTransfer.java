package com.erp.platform.modules.shareholder.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "share_transfers",
       indexes = {@Index(name = "idx_st_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_st_from", columnList = "from_shareholder_id"),
                  @Index(name = "idx_st_to", columnList = "to_shareholder_id")})
@Getter
@Setter
public class ShareTransfer extends TenantEntity {

    @Column(name = "transfer_number", length = 50, nullable = false)
    private String transferNumber;

    @Column(name = "from_shareholder_id")
    private UUID fromShareholderId;

    @Column(name = "from_shareholder_name", length = 200)
    private String fromShareholderName;

    @Column(name = "to_shareholder_id")
    private UUID toShareholderId;

    @Column(name = "to_shareholder_name", length = 200)
    private String toShareholderName;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(name = "shares_transferred", precision = 15, scale = 0, nullable = false)
    private BigDecimal sharesTransferred;

    @Column(name = "transfer_price_per_share", precision = 15, scale = 4)
    private BigDecimal transferPricePerShare;

    @Column(name = "stamp_duty", precision = 15, scale = 2)
    private BigDecimal stampDuty;

    @Column(name = "transfer_type", length = 30)
    private String transferType;

    @Column(name = "instrument_number", length = 100)
    private String instrumentNumber;

    /** PENDING / APPROVED / REJECTED */
    @Column(name = "status", length = 30)
    private String status = "PENDING";

    @Column(name = "remarks", length = 500)
    private String remarks;
}

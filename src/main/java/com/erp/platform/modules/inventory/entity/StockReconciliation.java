package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriStockReconciliation")
@Table(name = "agri_stock_reconciliations", indexes = {@Index(name = "idx_sr_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class StockReconciliation extends TenantEntity {

    @Column(name = "recon_number", length = 50, nullable = false)
    private String reconNumber;

    @Column(name = "recon_date")
    private LocalDate reconDate;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "material_code", length = 50)
    private String materialCode;

    @Column(name = "material_name", length = 200)
    private String materialName;

    @Column(name = "book_qty", precision = 15, scale = 3)
    private BigDecimal bookQty;

    @Column(name = "physical_qty", precision = 15, scale = 3)
    private BigDecimal physicalQty;

    @Column(name = "difference", precision = 15, scale = 3)
    private BigDecimal difference;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "status", length = 20)
    private String status = "DRAFT";
}

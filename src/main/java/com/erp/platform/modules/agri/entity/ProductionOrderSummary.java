package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity(name = "AgriProductionOrderSummary")
@Table(name = "agri_production_order_summaries", indexes = {@Index(name = "idx_pos_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProductionOrderSummary extends TenantEntity {

    @Column(name = "order_number", length = 50, nullable = false)
    private String orderNumber;

    @Column(name = "season", length = 50)
    private String season;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "variety_name", length = 100)
    private String varietyName;

    @Column(name = "planned_qty_kgs", precision = 15, scale = 3)
    private BigDecimal plannedQtyKgs;

    @Column(name = "actual_qty_kgs", precision = 15, scale = 3)
    private BigDecimal actualQtyKgs;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "processing_status", length = 50)
    private String processingStatus = "PENDING";

    @Column(name = "remarks", length = 500)
    private String remarks;
}

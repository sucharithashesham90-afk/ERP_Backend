package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity(name = "AgriProcessBom")
@Table(name = "agri_process_boms", indexes = {@Index(name = "idx_agri_process_bom_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProcessBom extends TenantEntity {

    @Column(name = "processing_type", length = 100, nullable = false)
    private String processingType;

    @Column(name = "item_code", length = 100)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "quantity", precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "bom_type", length = 50)
    private String bomType;

    @Column(name = "active")
    private boolean active = true;
}

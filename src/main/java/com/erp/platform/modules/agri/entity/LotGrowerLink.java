package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity(name = "AgriLotGrowerLink")
@Table(name = "agri_lot_grower_links", indexes = {
    @Index(name = "idx_lot_grower_link_tenant", columnList = "tenant_id")
})
@Getter
@Setter
public class LotGrowerLink extends TenantEntity {

    @Column(name = "link_number", length = 50, nullable = false)
    private String linkNumber;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "grower_code", length = 50)
    private String growerCode;

    @Column(name = "grower_name", length = 200)
    private String growerName;

    @Column(name = "village", length = 200)
    private String village;

    @Column(name = "organizer", length = 200)
    private String organizer;

    @Column(name = "contracted_qty_kgs", precision = 15, scale = 3)
    private BigDecimal contractedQtyKgs;

    @Column(name = "supplied_qty_kgs", precision = 15, scale = 3)
    private BigDecimal suppliedQtyKgs;

    @Column(name = "season", length = 50)
    private String season;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "remarks", length = 500)
    private String remarks;
}

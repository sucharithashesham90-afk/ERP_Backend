package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "intake_lines",
       indexes = {
           @Index(name = "idx_il_intake", columnList = "intake_id"),
           @Index(name = "idx_il_tenant", columnList = "tenant_id")
       })
@Getter
@Setter
public class IntakeLine extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_id", nullable = false)
    @JsonIgnore
    private ProductionIntake intake;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "product_category_name", length = 200)
    private String productCategoryName;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "material_state", length = 100)
    private String materialState;

    @Column(name = "storage_location_name", length = 200)
    private String storageLocationName;

    @Column(name = "net_bin_name", length = 200)
    private String netBinName;

    @Column(name = "pack_type", length = 50)
    private String packType;

    @Column(name = "pack_size", precision = 10, scale = 4)
    private BigDecimal packSize;

    @Column(name = "qty_per_bag", precision = 10, scale = 3)
    private BigDecimal qtyPerBag;

    @Column(name = "number_of_bags")
    private Integer numberOfBags;

    @Column(name = "total_quantity", precision = 20, scale = 4)
    private BigDecimal totalQuantity = BigDecimal.ZERO;

    @Column(name = "moisture_percent", precision = 5, scale = 2)
    private BigDecimal moisturePercent;

    @Column(name = "empty_bags_weight", precision = 10, scale = 3)
    private BigDecimal emptyBagsWeight = BigDecimal.ZERO;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "tests_selected", columnDefinition = "TEXT")
    private String testsSelected;

    @Column(name = "auto_slip_number", length = 50)
    private String autoSlipNumber;

    @Column(name = "lot_number", length = 50)
    private String lotNumber;
}

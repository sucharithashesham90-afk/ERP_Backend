package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crop_variety_tests", indexes = {@Index(name = "idx_crop_variety_test_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class CropVarietyTest extends TenantEntity {

    @Column(name = "crop_group_id", length = 100)
    private String cropGroupId;

    @Column(name = "crop_group_name", length = 150)
    private String cropGroupName;

    @Column(name = "crop_id", length = 100)
    private String cropId;

    @Column(name = "crop_name", length = 150)
    private String cropName;

    @Column(name = "variety_id", length = 100)
    private String varietyId;

    @Column(name = "variety_name", length = 150)
    private String varietyName;

    @Column(name = "seed_state_ids", length = 500)
    private String seedStateIds;

    @Column(name = "test_ids", length = 500)
    private String testIds;

    @Column(name = "test_names", length = 1000)
    private String testNames;

    @Column(name = "sample_quantity", precision = 18, scale = 4)
    private BigDecimal sampleQuantity;

    @Column(name = "sample_quantity_uom", length = 50)
    private String sampleQuantityUom;

    @Column(name = "update_inventory")
    private boolean updateInventory = false;

    @Column(name = "is_mandatory")
    private boolean mandatory = false;

    @Column(name = "process_seed_state_ids", length = 500)
    private String processSeedStateIds;

    @Column(name = "defined")
    private boolean defined = false;

    @Column(name = "active")
    private boolean active = true;

    @OneToMany(mappedBy = "cropVarietyTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CropVarietyTestLocationConfig> locationConfigs = new ArrayList<>();

    @OneToMany(mappedBy = "cropVarietyTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CropVarietyTestPropertyStandard> propertyStandards = new ArrayList<>();
}

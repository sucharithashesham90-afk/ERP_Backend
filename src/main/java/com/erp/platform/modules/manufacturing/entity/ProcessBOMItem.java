package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "process_bom_items",
       indexes = {
           @Index(name = "idx_pbomitem_bom", columnList = "process_bom_id")
       })
@Getter
@Setter
public class ProcessBOMItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_bom_id", nullable = false)
    @JsonIgnore
    private ProcessBOM processBom;

    @Column(name = "process_step_id")
    private UUID processStepId;

    @Column(name = "process_step_name", length = 200)
    private String processStepName;

    @Column(name = "product_category_name", length = 200)
    private String productCategoryName;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "bom_id")
    private UUID bomId;

    @Column(name = "bom_name", length = 200)
    private String bomName;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}

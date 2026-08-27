package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "producer_contracts",
       indexes = {@Index(name = "idx_prod_contract_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ProducerContract extends TenantEntity {

    @Column(nullable = false, length = 50)
    private String contractNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_producer_id")
    private FieldProducer fieldProducer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_variant_id")
    private PlantVariant plantVariant;

    @Column(nullable = false)
    private LocalDate contractDate;

    @Column
    private LocalDate expectedHarvestDate;

    /** Contracted area in acres */
    @Column(precision = 10, scale = 3)
    private BigDecimal contractedArea;

    /** Expected quantity in kg */
    @Column(precision = 15, scale = 3)
    private BigDecimal expectedQuantity;

    /** Agreed purchase rate per kg */
    @Column(precision = 10, scale = 2)
    private BigDecimal agreedRate;

    @Column(length = 30)
    private String status; // DRAFT, ACTIVE, COMPLETED, CANCELLED

    @Column(length = 100)
    private String season;

    @Column(length = 500)
    private String termsAndConditions;

    @Column(length = 200)
    private String remarks;
}

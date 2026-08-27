package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "units_of_measure",
       indexes = {
           @Index(name = "idx_uom_tenant", columnList = "tenant_id"),
           @Index(name = "idx_uom_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class UnitOfMeasure extends TenantEntity {

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String symbol;

    @Column(name = "uom_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UoMType uomType;

    @Column(name = "base_unit")
    private boolean baseUnit = false;

    @Column(name = "decimal_places")
    private int decimalPlaces = 2;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 1000)
    private String notes;

    public enum UoMType {
        QUANTITY, WEIGHT, VOLUME, LENGTH, AREA, TIME, CURRENCY
    }
}

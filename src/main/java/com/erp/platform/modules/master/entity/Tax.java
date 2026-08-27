package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tax_rates",
       indexes = {@Index(name = "idx_tax_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Tax extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "tax_type", length = 20)
    @Enumerated(EnumType.STRING)
    private TaxType taxType = TaxType.PERCENTAGE;

    // The tax category shown in the UI (GST / IGST / CGST / SGST / VAT / NONE).
    @Column(name = "type", length = 20)
    private String type;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "is_compound")
    private boolean compound = false;

    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public enum TaxType {
        PERCENTAGE, FIXED
    }
}

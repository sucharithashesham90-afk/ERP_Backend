package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "vat_definitions",
       indexes = {
           @Index(name = "idx_vd_tenant", columnList = "tenant_id"),
           @Index(name = "idx_vd_code",   columnList = "tenant_id,code", unique = true)
       })
@Getter
@Setter
public class VatDefinition extends TenantEntity {

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(precision = 8, scale = 4)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "cgst_rate", precision = 8, scale = 4)
    private BigDecimal cgstRate = BigDecimal.ZERO;

    @Column(name = "sgst_rate", precision = 8, scale = 4)
    private BigDecimal sgstRate = BigDecimal.ZERO;

    @Column(name = "igst_rate", precision = 8, scale = 4)
    private BigDecimal igstRate = BigDecimal.ZERO;

    @Column(name = "cess_rate", precision = 8, scale = 4)
    private BigDecimal cessRate = BigDecimal.ZERO;

    @Column(name = "hsn_code", length = 30)
    private String hsnCode;

    @Column(name = "tax_type", length = 20)
    private String taxType; // IGST, CGST, SGST, CESS, VAT, GST

    @Column(name = "account_code", length = 30)
    private String accountCode; // GL account for this tax

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}

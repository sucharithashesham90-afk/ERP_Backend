package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_methods",
       indexes = {@Index(name = "idx_paymethod_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PaymentMethod extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(length = 30)
    private String type; // CASH, CHEQUE, NEFT, RTGS, UPI, CARD, DD

    @Column(length = 500)
    private String description;

    @Column(name = "bank_required")
    private boolean bankRequired = false;

    @Column(name = "reference_required")
    private boolean referenceRequired = false;

    @Column(nullable = false)
    private boolean active = true;
}

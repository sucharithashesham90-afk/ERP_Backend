package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AgriPricingMethod")
@Table(name = "pricing_methods", indexes = {@Index(name = "idx_pricing_method_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PricingMethod extends TenantEntity {

    @Column(name = "method_name", length = 200, nullable = false)
    private String methodName;

    @Column(name = "seed_type", length = 100)
    private String seedType;

    @Column(name = "procurement_seed_state", length = 100)
    private String procurementSeedState;

    @Column(name = "liability_for_payment", length = 50)
    private String liabilityForPayment;

    @Column(name = "liability_payment_to", length = 50)
    private String liabilityPaymentTo;

    @Column(name = "weight_deductions_mode", length = 50)
    private String weightDeductionsMode;

    @Column(name = "cost_deductions_mode", length = 50)
    private String costDeductionsMode;

    @Column(name = "sealing_charges")
    private boolean sealingCharges;

    @Column(name = "revenue_stamp")
    private boolean revenueStamp;

    @Column(name = "advance_from_grower")
    private boolean advanceFromGrower;

    @Column(name = "pricing_based_on_seed_state", length = 100)
    private String pricingBasedOnSeedState;

    @Column(name = "quality_testing", length = 50)
    private String qualityTesting;

    @Column(name = "processing_step_id")
    private java.util.UUID processingStepId;

    @Column(name = "processing_step_name", length = 200)
    private String processingStepName;

    @Column(name = "active")
    private boolean active = true;
}

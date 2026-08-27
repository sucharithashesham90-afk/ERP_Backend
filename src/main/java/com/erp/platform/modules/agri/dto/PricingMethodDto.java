package com.erp.platform.modules.agri.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PricingMethodDto {
    private UUID id;
    private String methodName;
    private String procurementSeedState;
    private String pricingBasedOnSeedState;
    private String liabilityForPayment;
    private UUID processingStepId;
    private String processingStepName;
    private String liabilityPaymentTo;
    private String qualityTesting;
    private boolean active;
    private LocalDateTime createdAt;
}

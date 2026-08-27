package com.erp.platform.modules.agri.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePricingMethodRequest {

    @NotBlank
    private String methodName;

    private String procurementSeedState;
    private String pricingBasedOnSeedState;
    private String liabilityForPayment;
    // String (not UUID) so a blank value from the form doesn't fail JSON binding; parsed in the service.
    private String processingStepId;
    private String processingStepName;
    private String liabilityPaymentTo;
    private String qualityTesting;
    private boolean active = true;
}

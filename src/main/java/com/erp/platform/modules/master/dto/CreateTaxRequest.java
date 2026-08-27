package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.Tax.TaxType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTaxRequest {

    @NotBlank(message = "Tax name is required")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Tax type is required")
    private TaxType taxType = TaxType.PERCENTAGE;

    // Tax category from the UI (GST / IGST / CGST / SGST / VAT / NONE).
    private String type;

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.0")
    private BigDecimal rate = BigDecimal.ZERO;

    private boolean compound = false;

    @Size(max = 500)
    private String description;
}

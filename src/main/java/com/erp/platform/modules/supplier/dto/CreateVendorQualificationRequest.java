package com.erp.platform.modules.supplier.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/** Every field here is mandatory — a vendor qualification with a blank score/date used to reach
 *  VendorQualificationService.apply() as an empty string and NPE/parse-fail into a raw 500. */
@Data
public class CreateVendorQualificationRequest {

    @NotNull(message = "Vendor is required")
    private UUID vendorId;

    private String vendorName;

    @NotNull(message = "Technical score is required")
    @Min(0) @Max(100)
    private Integer technicalScore;

    @NotNull(message = "Financial score is required")
    @Min(0) @Max(100)
    private Integer financialScore;

    @NotNull(message = "Compliance score is required")
    @Min(0) @Max(100)
    private Integer complianceScore;

    @NotBlank(message = "Certifications are required")
    private String certifications;

    private String evaluatedBy;

    @NotNull(message = "Valid until date is required")
    private LocalDate validUntil;

    private String notes;
}

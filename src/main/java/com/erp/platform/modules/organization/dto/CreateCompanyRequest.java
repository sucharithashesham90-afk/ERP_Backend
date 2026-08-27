package com.erp.platform.modules.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String legalName;

    @Size(max = 50)
    private String registrationNumber;

    @Size(max = 50)
    private String taxNumber;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 200)
    private String website;

    @Size(max = 500)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 20)
    private String gstin;

    @Size(max = 10)
    private String pan;

    @Size(max = 21)
    private String cin;

    @Size(max = 10)
    private String tan;

    @Size(max = 100)
    private String bankName;

    @Size(max = 30)
    private String bankAccount;

    @Size(max = 11)
    private String bankIFSC;

    @Size(max = 100)
    private String bankBranch;

    @Size(max = 2)
    private String financialYearStart;

    /**
     * The company's logo, held as a data URI rather than a link.
     *
     * <p>There was a 500-character cap here from when this was a URL. The API returns the logo in
     * this field, so once one had been uploaded every subsequent save sent tens of kilobytes back
     * and was refused — the company could not be edited at all until the logo was cleared. The
     * column behind it is text; the size of an upload is capped in the browser instead.
     *
     * <p>logo and logoUrl are both accepted because the screen sends the first and the API returns
     * the second, and a field that is written under one name and read under another is how the
     * upload was being lost.
     */
    private String logoUrl;

    private String logo;

    /** Nullable on purpose: absent means leave the current setting alone, not disable it. */
    private Boolean pdfWatermarkEnabled;

    @Size(max = 50)
    private String industry;

    @Size(max = 10)
    private String currency = "INR";
}

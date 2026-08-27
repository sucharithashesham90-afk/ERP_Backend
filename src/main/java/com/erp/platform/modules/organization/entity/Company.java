package com.erp.platform.modules.organization.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "companies",
       indexes = {@Index(name = "idx_company_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Company extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "legal_name", length = 200)
    private String legalName;

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "tax_number", length = 50)
    private String taxNumber;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String website;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    /**
     * The company's own logo, as a data URI.
     *
     * <p>Held as text rather than varchar(500): a base64 PNG of even a small mark runs to tens of
     * thousands of characters, so the old 500-character column could only ever have stored a URL.
     * Uploads are size-capped in the browser before they reach here.
     */
    @Column(columnDefinition = "text")
    private String logo;

    /**
     * Whether exported PDFs carry the logo as a watermark. On by default — a document that leaves
     * the system should say whose it is — but some clients print onto pre-printed stationery, so it
     * has to be possible to turn off.
     */
    /*
     * The default is spelled out for the database, not just for Java. Adding a NOT NULL column with
     * no default to a table that already has rows is rejected by Postgres, and Hibernate's
     * ddl-auto=update logs that rejection as a warning and carries on booting. The application then
     * runs normally against a table missing the column, and every insert and update of a company
     * fails with "column pdf_watermark_enabled does not exist". With a default the ALTER is legal:
     * existing rows are backfilled, then NOT NULL is applied.
     */
    @Column(name = "pdf_watermark_enabled", nullable = false, columnDefinition = "boolean default true")
    private boolean pdfWatermarkEnabled = true;

    @Column(length = 20)
    private String gstin;

    @Column(length = 10)
    private String pan;

    @Column(length = 21)
    private String cin;

    @Column(length = 10)
    private String tan;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account", length = 30)
    private String bankAccount;

    @Column(name = "bank_ifsc", length = 11)
    private String bankIFSC;

    @Column(name = "bank_branch", length = 100)
    private String bankBranch;

    @Column(name = "financial_year_start", length = 2)
    private String financialYearStart = "04";

    @Column(length = 50)
    private String industry;

    @Column(length = 10)
    private String currency = "INR";

    @Column(nullable = false)
    private boolean active = true;
}

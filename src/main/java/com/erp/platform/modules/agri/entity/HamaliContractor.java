package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity(name = "AgriHamaliContractor")
@Table(name = "agri_hamali_contractors", indexes = {
    @Index(name = "idx_hamali_contractor_tenant", columnList = "tenant_id")
})
@Getter
@Setter
public class HamaliContractor extends TenantEntity {

    @Column(name = "contractor_code", length = 50, nullable = false)
    private String contractorCode;

    @Column(name = "contractor_name", length = 200)
    private String contractorName;

    @Column(name = "contractor_type", length = 100)
    private String contractorType;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "address", length = 500)
    private String address;

    // The rest of the address. Ids and names are both stored: the id is what the cascading
    // dropdowns reopen against, the name is what a report or a printed docket needs without
    // three extra lookups.
    @Column(name = "state_id")
    private UUID stateId;

    @Column(name = "state_name", length = 150)
    private String stateName;

    @Column(name = "district_id")
    private UUID districtId;

    @Column(name = "district_name", length = 150)
    private String districtName;

    @Column(name = "mandal_id")
    private UUID mandalId;

    @Column(name = "mandal_name", length = 150)
    private String mandalName;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(name = "rate_per_bag", precision = 10, scale = 2)
    private BigDecimal ratePerBag;

    @Column(name = "rate_per_kg", precision = 10, scale = 4)
    private BigDecimal ratePerKg;

    @Column(name = "season", length = 50)
    private String season;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "active")
    private Boolean active = true;
}

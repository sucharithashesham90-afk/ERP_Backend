package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.Vendor.VendorCategory;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class VendorDto {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String code;
    private String email;
    private String phone;
    private String mobile;
    private String contactPerson;
    private String address;
    private String address2;
    private String district;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String taxNumber;
    private String panNumber;
    private String fax;
    private String bankName;
    private String bankAccount;
    private String bankRoutingCode;
    private BigDecimal outstandingBalance;
    private VendorCategory category;
    private Integer paymentTermsDays;
    private String notes;
    private boolean active;
    private LocalDateTime createdAt;
    private String photo;
}

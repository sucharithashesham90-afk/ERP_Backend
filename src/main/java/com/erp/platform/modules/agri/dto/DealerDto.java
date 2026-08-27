package com.erp.platform.modules.agri.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DealerDto {
    private UUID id;
    private String name;
    private String code;
    private String contactPerson;
    private String phone;
    private String mobile;
    private String email;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String dealerType;
    private UUID dealerRegionId;
    private String dealerRegionName;
    private BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    private String accountCode;
    private String licenseNumber;
    private String panNumber;
    private String gstNumber;
    private String bankAccount;
    private String bankIfscCode;
    private String bankName;
    private boolean active;
    private LocalDateTime createdAt;
}

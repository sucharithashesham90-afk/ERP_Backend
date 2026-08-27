package com.erp.platform.modules.auth.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String username;

    // Extended profile fields
    private String title;
    private String phone;
    private String mobile;
    private String fax;
    private String officeAddress;
    private String city;
    private String state;
    private String zip;
    private String salesRepId;
    private Integer privilegeLevel;
    private Boolean changePasswordOnLogin;
    private LocalDate activationDate;
    private LocalDate expiryDate;
}

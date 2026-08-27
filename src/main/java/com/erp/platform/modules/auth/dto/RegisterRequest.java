package com.erp.platform.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class RegisterRequest {

    @Size(max = 150)
    private String fullName;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    private UUID tenantId;
    private UUID groupId;
    private String roleName;
    private List<String> roles;

    // Extended profile fields
    private String title;
    private String mobile;
    private String fax;
    private String officeAddress;
    private String city;
    private String state;
    private String zip;
    private String salesRepId;
    private Integer privilegeLevel;
    private boolean changePasswordOnLogin;
    private LocalDate activationDate;
    private LocalDate expiryDate;
    private UUID reportingManagerId;
}

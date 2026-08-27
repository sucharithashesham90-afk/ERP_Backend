package com.erp.platform.modules.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String mobile;

    @Size(max = 10)
    private String gender;

    private LocalDate dateOfBirth;

    @Size(max = 100)
    private String designation;

    @Size(max = 20)
    private String employmentType;

    private LocalDate joiningDate;

    private UUID departmentId;
    private UUID branchId;
    private UUID managerId;
    private UUID userId;
    private Set<UUID> groupIds;

    /**
     * The default password HR sets when creating this employee — provisions their login in the
     * same request (required in {@code EmployeeService.create()}, not here, since this DTO is
     * reused by update() where a login already exists and shouldn't be touched).
     */
    private String defaultPassword;

    /** The Role (by name) assigned to that new login — determines which modules/screens it can see. */
    private String roleName;

    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Size(max = 500)
    private String address;

    @Size(max = 100)
    private String district;
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String nationality;

    @Size(max = 100)
    private String country;

    private LocalDate confirmationDate;
    private LocalDate resignationDate;

    @Size(max = 20)
    private String status;

    @Size(max = 100)
    private String bankName;

    @Size(max = 30)
    private String bankAccount;

    @Size(max = 20)
    private String bankIFSC;

    @Size(max = 20)
    private String panNumber;

    @Size(max = 30)
    private String pfNumber;

    @Size(max = 30)
    private String esiNumber;

    // Extended personal details
    @Size(max = 10)
    private String title;

    @Size(max = 100)
    private String fatherName;

    @Size(max = 20)
    private String fax;

    @Size(max = 30)
    private String passportNumber;

    @Size(max = 200)
    private String qualification;

    @Size(max = 200)
    private String previousCompany;

    private Integer experienceYears;

    // Permanent address extended
    @Size(max = 500)
    private String address2;

    @Size(max = 20)
    private String zipCode;

    // Temporary address
    @Size(max = 500)
    private String tempAddress;

    @Size(max = 500)
    private String tempAddress2;

    @Size(max = 100)
    private String tempDistrict;
    private String tempCountry;
    private String tempCity;

    @Size(max = 100)
    private String tempState;

    @Size(max = 20)
    private String tempZip;
    private String photo;
}

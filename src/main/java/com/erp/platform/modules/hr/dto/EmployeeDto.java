package com.erp.platform.modules.hr.dto;

import com.erp.platform.modules.auth.entity.User.UserStatus;
import com.erp.platform.modules.hr.entity.Employee.EmployeeStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class EmployeeDto {
    private UUID id;
    private UUID tenantId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String mobile;
    private String gender;
    private LocalDate dateOfBirth;
    private String designation;
    private String employmentType;
    private LocalDate joiningDate;
    private EmployeeStatus status;
    private UUID departmentId;
    private UUID branchId;
    private UUID managerId;
    private UUID userId;
    private Set<UUID> groupIds;
    /** The linked login's approval state (null until a login has been provisioned for this employee). */
    private UserStatus userStatus;
    /** The linked login's assigned role, read at fetch time — same purpose as userStatus above. */
    private String roleName;
    private BigDecimal basicSalary;
    private String address;
    private String district;
    private String city;
    private String state;
    private String country;
    private LocalDate confirmationDate;
    private LocalDate resignationDate;
    private String nationality;
    private String bankName;
    private String bankAccount;
    private String bankIFSC;
    private String panNumber;
    private String pfNumber;
    private String esiNumber;

    // Extended personal details
    private String title;
    private String fatherName;
    private String fax;
    private String passportNumber;
    private String qualification;
    private String previousCompany;
    private Integer experienceYears;

    // Permanent address extended
    private String address2;
    private String zipCode;

    // Temporary address
    private String tempAddress;
    private String tempAddress2;
    private String tempDistrict;
    private String tempCountry;
    private String tempCity;
    private String tempState;
    private String tempZip;

    private LocalDateTime createdAt;
    private String photo;
}

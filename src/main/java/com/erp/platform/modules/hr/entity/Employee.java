package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "employees",
       indexes = {
           @Index(name = "idx_emp_tenant", columnList = "tenant_id"),
           @Index(name = "idx_emp_code", columnList = "tenant_id, employee_code")
       })
@Getter
@Setter
public class Employee extends TenantEntity {

    @Column(name = "employee_code", nullable = false, length = 30)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String mobile;

    @Column(name = "photo", columnDefinition = "text")
    private String photo;

    @Column(length = 10)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 50)
    private String nationality;

    @Column(length = 500)
    private String address;

    /** Sits between state and city, matching the districts master. */
    @Column(length = 100)
    private String district;

    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String designation;

    @Column(name = "employment_type", length = 20)
    private String employmentType;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "branch_id")
    private UUID branchId;

    /** This employee's reporting manager (another Employee) — drives approval routing for
     *  provisioning, and (once assigned) leave approval. */
    @Column(name = "manager_id")
    private UUID managerId;

    @Column(name = "user_id")
    private UUID userId;

    /** Groups this employee belongs to — synced onto the linked User login by EmployeeService. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_groups", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "group_id")
    private Set<UUID> groupIds = new HashSet<>();

    @Column(name = "basic_salary", precision = 18, scale = 2)
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account", length = 30)
    private String bankAccount;

    @Column(name = "bank_ifsc", length = 20)
    private String bankIFSC;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "pf_number", length = 30)
    private String pfNumber;

    @Column(name = "esi_number", length = 30)
    private String esiNumber;

    // Extended personal details (from legacy JSP)
    @Column(length = 10)
    private String title;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(length = 20)
    private String fax;

    @Column(name = "passport_number", length = 30)
    private String passportNumber;

    @Column(length = 200)
    private String qualification;

    @Column(name = "previous_company", length = 200)
    private String previousCompany;

    @Column(name = "experience_years")
    private Integer experienceYears;

    // Permanent address (extended)
    @Column(name = "address2", length = 500)
    private String address2;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    // Temporary address
    @Column(name = "temp_address", length = 500)
    private String tempAddress;

    @Column(name = "temp_address2", length = 500)
    private String tempAddress2;

    @Column(name = "temp_district", length = 100)
    private String tempDistrict;

    @Column(name = "temp_country", length = 100)
    private String tempCountry;

    @Column(name = "temp_city", length = 100)
    private String tempCity;

    @Column(name = "temp_state", length = 100)
    private String tempState;

    @Column(name = "temp_zip", length = 20)
    private String tempZip;

    public enum EmployeeStatus {
        ACTIVE, PROBATION, RESIGNED, TERMINATED
    }
}

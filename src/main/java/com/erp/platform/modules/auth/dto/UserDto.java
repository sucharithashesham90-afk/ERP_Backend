package com.erp.platform.modules.auth.dto;

import com.erp.platform.modules.auth.entity.User.UserStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private UUID tenantId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private List<String> roles;
    private UUID groupId;
    private String groupName;
    private Set<UUID> groupIds;
    /** True when an Employee record links to this login — Groups/Reporting Manager are then
     *  edited on the Employee screen and synced here, not editable directly. */
    private boolean linkedToEmployee;
    private List<String> allowedLocations;
    private boolean active;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

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
    private boolean changePasswordOnLogin;
    private LocalDateTime passwordExpiresAt;
    private LocalDate activationDate;
    private LocalDate expiryDate;
    private UUID reportingManagerId;
    private String reportingManagerName;
    private String approvalRemarks;
    private UUID approvedBy;
    private LocalDateTime approvedAt;
}

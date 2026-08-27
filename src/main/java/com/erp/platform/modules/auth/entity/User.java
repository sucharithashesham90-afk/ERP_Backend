package com.erp.platform.modules.auth.entity;

import com.erp.platform.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends AuditableEntity {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String username;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Column(length = 20)
    private String title;

    @Column(length = 20)
    private String mobile;

    @Column(length = 20)
    private String fax;

    @Column(name = "office_address", length = 300)
    private String officeAddress;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String zip;

    @Column(name = "sales_rep_id", length = 50)
    private String salesRepId;

    @Column(name = "privilege_level")
    private Integer privilegeLevel;

    @Column(name = "change_password_on_login")
    private boolean changePasswordOnLogin = false;

    /** When a temp password (set via employee login provisioning) stops being valid — null means
     *  no expiry gate applies, same as for every account created outside that flow. */
    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    @Column(name = "activation_date")
    private java.time.LocalDate activationDate;

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    /** The primary group — kept in sync with the first entry of {@link #groupIds} for the sake of
     *  existing single-group call sites (role filtering, member counts). */
    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "group_name", length = 100)
    private String groupName;

    /** Full multi-group membership. When an Employee is linked, this is synced from the
     *  Employee's own groupIds — see EmployeeService. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_groups_multi", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "group_id")
    private Set<UUID> groupIds = new HashSet<>();

    @Column(name = "reporting_manager_id")
    private UUID reportingManagerId;

    @Column(name = "reporting_manager_name", length = 150)
    private String reportingManagerName;

    @Column(name = "approval_remarks", length = 500)
    private String approvalRemarks;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_locations", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "location_name", length = 150)
    private Set<String> allowedLocations = new HashSet<>();

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public enum UserStatus {
        PENDING_APPROVAL, ACTIVE, INACTIVE, LOCKED, REJECTED
    }
}

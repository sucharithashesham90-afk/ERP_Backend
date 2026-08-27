package com.erp.platform.modules.auth.repository;

import com.erp.platform.modules.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);
    boolean existsByEmail(String email);
    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
    Page<User> findByTenantId(UUID tenantId, Pageable pageable);
    long countByGroupId(UUID groupId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.expiryDate IS NOT NULL AND u.expiryDate BETWEEN :today AND :cutoff")
    Page<User> findExpiringUsers(UUID tenantId, java.time.LocalDate today, java.time.LocalDate cutoff, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
        "SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.expiryDate IS NOT NULL AND u.expiryDate < :today")
    Page<User> findExpiredUsers(UUID tenantId, java.time.LocalDate today, Pageable pageable);

    /** Users pending approval — visible to all approvers in the tenant. */
    @org.springframework.data.jpa.repository.Query(
        "SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.status = 'PENDING_APPROVAL'")
    Page<User> findPendingApprovals(UUID tenantId, Pageable pageable);

    /** Users pending approval for a specific reporting manager. */
    @org.springframework.data.jpa.repository.Query(
        "SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.status = 'PENDING_APPROVAL' AND u.reportingManagerId = :managerId")
    Page<User> findPendingApprovalsForManager(UUID tenantId, UUID managerId, Pageable pageable);
}

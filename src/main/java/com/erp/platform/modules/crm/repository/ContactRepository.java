package com.erp.platform.modules.crm.repository;

import com.erp.platform.modules.crm.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {
    Page<Contact> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<Contact> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}

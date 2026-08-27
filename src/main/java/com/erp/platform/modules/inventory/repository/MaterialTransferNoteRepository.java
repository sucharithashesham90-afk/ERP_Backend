package com.erp.platform.modules.inventory.repository;

import com.erp.platform.modules.inventory.entity.MaterialTransferNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MaterialTransferNoteRepository extends JpaRepository<MaterialTransferNote, UUID> {

    Page<MaterialTransferNote> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<MaterialTransferNote> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}

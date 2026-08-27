package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.PayGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayGradeRepository extends JpaRepository<PayGrade, UUID> {

    List<PayGrade> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<PayGrade> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    Page<PayGrade> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
}

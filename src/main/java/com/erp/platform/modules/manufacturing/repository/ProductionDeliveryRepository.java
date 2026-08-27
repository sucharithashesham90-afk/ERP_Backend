package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProductionDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionDeliveryRepository extends JpaRepository<ProductionDelivery, UUID>,
        JpaSpecificationExecutor<ProductionDelivery> {
    Page<ProductionDelivery> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<ProductionDelivery> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    /**
     * Filtered delivery search, built as a Specification for the same reason as the intake one:
     * the previous {@code (:param IS NULL OR ...)} form bound a NULL for every unused filter, and
     * the Manufacturing MIS calls this with only a date range on every run.
     */
    static Specification<ProductionDelivery> search(UUID tenantId, String status, String lotNumber,
                                                    String jobNumber, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> and = new ArrayList<>();
            and.add(cb.equal(root.get("tenantId"), tenantId));
            and.add(cb.isNull(root.get("deletedAt")));
            if (has(status)) and.add(cb.equal(root.get("status"), status));
            if (has(lotNumber)) {
                and.add(cb.like(cb.lower(root.get("lotNumber")), "%" + lotNumber.toLowerCase() + "%"));
            }
            if (has(jobNumber)) {
                and.add(cb.like(cb.lower(root.get("jobNumber")), "%" + jobNumber.toLowerCase() + "%"));
            }
            if (from != null) and.add(cb.greaterThanOrEqualTo(root.get("deliveryDate"), from));
            if (to != null) and.add(cb.lessThanOrEqualTo(root.get("deliveryDate"), to));
            return cb.and(and.toArray(new Predicate[0]));
        };
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }
}

package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProductionIntake;
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

public interface ProductionIntakeRepository extends JpaRepository<ProductionIntake, UUID>,
        JpaSpecificationExecutor<ProductionIntake> {
    Page<ProductionIntake> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Optional<ProductionIntake> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
    List<ProductionIntake> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);

    /**
     * Filtered intake search, built as a Specification rather than a fixed JPQL string.
     *
     * <p>The previous query tested every filter with {@code (:param IS NULL OR ...)}, which binds a
     * NULL for each unused filter and leaves PostgreSQL to infer a type it cannot always work out.
     * The Manufacturing MIS calls this with only a date range on every run, so it was one of the
     * three calls that had to succeed for that screen to load at all.
     *
     * <p>A Specification adds a predicate only for the filters actually supplied, so an unused
     * filter binds nothing.
     */
    static Specification<ProductionIntake> search(UUID tenantId, String intakeType, String status,
                                                  String lotNumber, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> and = new ArrayList<>();
            and.add(cb.equal(root.get("tenantId"), tenantId));
            and.add(cb.isNull(root.get("deletedAt")));
            if (has(intakeType)) and.add(cb.equal(root.get("intakeType"), intakeType));
            if (has(status)) and.add(cb.equal(root.get("status"), status));
            if (has(lotNumber)) {
                and.add(cb.like(cb.lower(root.get("lotNumber")), "%" + lotNumber.toLowerCase() + "%"));
            }
            if (from != null) and.add(cb.greaterThanOrEqualTo(root.get("intakeDate"), from));
            if (to != null) and.add(cb.lessThanOrEqualTo(root.get("intakeDate"), to));
            return cb.and(and.toArray(new Predicate[0]));
        };
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }
}

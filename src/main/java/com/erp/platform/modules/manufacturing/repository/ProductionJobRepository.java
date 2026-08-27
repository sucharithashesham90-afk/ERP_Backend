package com.erp.platform.modules.manufacturing.repository;

import com.erp.platform.modules.manufacturing.entity.ProductionJob;
import com.erp.platform.modules.manufacturing.entity.ProductionJob.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionJobRepository extends JpaRepository<ProductionJob, UUID>,
        JpaSpecificationExecutor<ProductionJob> {

    Page<ProductionJob> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<ProductionJob> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, JobStatus status, Pageable pageable);

    Page<ProductionJob> findByTenantIdAndProcessTypeAndDeletedAtIsNull(UUID tenantId, String processType, Pageable pageable);

    Optional<ProductionJob> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<ProductionJob> findByTenantIdAndLotNumberAndDeletedAtIsNull(UUID tenantId, String lotNumber);

    @Query("SELECT j FROM ProductionJob j WHERE j.tenantId = :tenantId AND j.deletedAt IS NULL AND (j.locationName IN :locations OR j.locationName IS NULL)")
    Page<ProductionJob> findByTenantIdAndLocationsAndDeletedAtIsNull(@Param("tenantId") UUID tenantId, @Param("locations") List<String> locations, Pageable pageable);

    @Query("SELECT j FROM ProductionJob j WHERE j.tenantId = :tenantId AND j.status = :status AND j.deletedAt IS NULL AND (j.locationName IN :locations OR j.locationName IS NULL)")
    Page<ProductionJob> findByTenantIdAndStatusAndLocationsAndDeletedAtIsNull(@Param("tenantId") UUID tenantId, @Param("status") JobStatus status, @Param("locations") List<String> locations, Pageable pageable);

    @Query("SELECT j FROM ProductionJob j WHERE j.tenantId = :tenantId AND j.processType = :processType AND j.deletedAt IS NULL AND (j.locationName IN :locations OR j.locationName IS NULL)")
    Page<ProductionJob> findByTenantIdAndProcessTypeAndLocationsAndDeletedAtIsNull(@Param("tenantId") UUID tenantId, @Param("processType") String processType, @Param("locations") List<String> locations, Pageable pageable);

    /**
     * Filtered job search, built as a Specification rather than a fixed JPQL string.
     *
     * <p>The previous query tested every filter with {@code (:param IS NULL OR ...)}, which means
     * binding a NULL for each unused filter. For the string and date parameters PostgreSQL can infer
     * a type; for {@code :status}, an enum, it cannot, and it fails the whole statement with "could
     * not determine data type of parameter". The plain job list never hit it because it only takes
     * this path once a filter is set, but the Comprehensive Processing Report and the Manufacturing
     * MIS both send a date range every time, so both failed on every run.
     *
     * <p>A Specification adds a predicate only for the filters that were actually supplied, so an
     * unused filter binds nothing at all. It also fixes a precedence bug that was hiding in the
     * {@code workCenter} clause: {@code A IS NULL OR x LIKE A OR y LIKE A} was grouped so that a row
     * matching {@code processLineName} came back whatever the other filters said.
     */
    static Specification<ProductionJob> search(
            UUID tenantId, JobStatus status, String processType,
            String jobNumber, String locationName, String workCenter,
            String storageLocation, String lotNumber,
            LocalDate startFrom, LocalDate startTo,
            LocalDate endFrom, LocalDate endTo) {

        return (root, query, cb) -> {
            List<Predicate> and = new ArrayList<>();
            and.add(cb.equal(root.get("tenantId"), tenantId));
            and.add(cb.isNull(root.get("deletedAt")));

            if (status != null) and.add(cb.equal(root.get("status"), status));
            if (has(processType)) and.add(cb.equal(root.get("processType"), processType));
            if (has(jobNumber)) and.add(like(cb, root.get("jobNumber"), jobNumber));
            if (has(locationName)) and.add(like(cb, root.get("locationName"), locationName));
            if (has(storageLocation)) and.add(like(cb, root.get("storageLocationName"), storageLocation));
            if (has(lotNumber)) and.add(like(cb, root.get("lotNumber"), lotNumber));

            // Work centre matches either column - kept as one bracketed OR so it narrows the
            // result set instead of widening it.
            if (has(workCenter)) {
                and.add(cb.or(like(cb, root.get("workCenter"), workCenter),
                              like(cb, root.get("processLineName"), workCenter)));
            }

            if (startFrom != null) and.add(cb.greaterThanOrEqualTo(root.get("plannedStartDate"), startFrom));
            if (startTo != null) and.add(cb.lessThanOrEqualTo(root.get("plannedStartDate"), startTo));
            if (endFrom != null) and.add(cb.greaterThanOrEqualTo(root.get("plannedEndDate"), endFrom));
            if (endTo != null) and.add(cb.lessThanOrEqualTo(root.get("plannedEndDate"), endTo));

            return cb.and(and.toArray(new Predicate[0]));
        };
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }

    private static Predicate like(CriteriaBuilder cb, Path<String> path, String value) {
        return cb.like(cb.lower(path), "%" + value.toLowerCase() + "%");
    }
}

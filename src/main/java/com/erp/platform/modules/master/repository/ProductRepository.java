package com.erp.platform.modules.master.repository;

import com.erp.platform.modules.master.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Optional<Product> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    boolean existsByTenantIdAndCodeAndDeletedAtIsNull(UUID tenantId, String code);

    // Barcode-scan lookup: match the scanned value against barcode, SKU or product code.
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.deletedAt IS NULL " +
           "AND (LOWER(p.barcode) = LOWER(:code) OR LOWER(p.sku) = LOWER(:code) OR LOWER(p.code) = LOWER(:code))")
    List<Product> findByScanCode(@Param("tenantId") UUID tenantId, @Param("code") String code);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.deletedAt IS NULL " +
           "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(p.code) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(p.sku) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))")
    Page<Product> searchByTenantId(@Param("tenantId") UUID tenantId,
                                   @Param("search") String search,
                                   Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.deletedAt IS NULL " +
           "AND p.productType IN :types")
    Page<Product> findByTenantIdAndProductTypeIn(@Param("tenantId") UUID tenantId,
                                                  @Param("types") List<Product.ProductType> types,
                                                  Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.deletedAt IS NULL " +
           "AND p.productType IN :types " +
           "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')) " +
           "     OR LOWER(p.code) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))")
    Page<Product> searchByTenantIdAndProductTypeIn(@Param("tenantId") UUID tenantId,
                                                    @Param("search") String search,
                                                    @Param("types") List<Product.ProductType> types,
                                                    Pageable pageable);
}

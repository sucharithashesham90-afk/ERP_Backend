package com.erp.platform.modules.accounting.repository;

import com.erp.platform.modules.accounting.entity.BankVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankVoucherRepository extends JpaRepository<BankVoucher, UUID>, JpaSpecificationExecutor<BankVoucher> {

    Optional<BankVoucher> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    List<BankVoucher> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    List<BankVoucher> findByTenantIdAndVoucherTypeAndVoucherDateBetweenAndDeletedAtIsNull(
            UUID tenantId, BankVoucher.VoucherType voucherType, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(v.totalAmount), 0) FROM BankVoucher v " +
           "WHERE v.tenantId = :tenantId AND v.voucherType = :type AND v.voucherDate < :before AND v.deletedAt IS NULL")
    BigDecimal sumBeforeDate(@Param("tenantId") UUID tenantId,
                             @Param("type") BankVoucher.VoucherType type,
                             @Param("before") LocalDate before);
}

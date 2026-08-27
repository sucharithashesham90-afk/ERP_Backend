package com.erp.platform.modules.expense.repository;

import com.erp.platform.modules.expense.entity.Expense;
import com.erp.platform.modules.expense.entity.Expense.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    Page<Expense> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<Expense> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, ExpenseStatus status, Pageable pageable);

    Page<Expense> findByTenantIdAndEmployeeIdAndDeletedAtIsNull(UUID tenantId, UUID employeeId, Pageable pageable);

    Optional<Expense> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(e.expenseNumber, 5) AS int)), 0) FROM Expense e WHERE e.tenantId = :tenantId AND e.deletedAt IS NULL")
    int maxExpenseSeq(@Param("tenantId") UUID tenantId);
}

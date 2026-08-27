package com.erp.platform.modules.hr.repository;

import com.erp.platform.modules.hr.entity.Expense;
import com.erp.platform.modules.hr.entity.Expense.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HrExpenseRepository extends JpaRepository<Expense, UUID> {

    Page<Expense> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    Page<Expense> findByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, ExpenseStatus status, Pageable pageable);

    Page<Expense> findByTenantIdAndEmployeeIdAndDeletedAtIsNull(UUID tenantId, UUID employeeId, Pageable pageable);

    Optional<Expense> findByTenantIdAndIdAndDeletedAtIsNull(UUID tenantId, UUID id);
}

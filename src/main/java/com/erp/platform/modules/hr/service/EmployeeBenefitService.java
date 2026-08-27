package com.erp.platform.modules.hr.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.entity.EmployeeBenefit;
import com.erp.platform.modules.hr.repository.EmployeeBenefitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeBenefitService {

    private final EmployeeBenefitRepository employeeBenefitRepository;
    private final TenantContext tenantContext;

    public PageResponse<Map<String, Object>> list(Pageable pageable) {
        return PageResponse.of(employeeBenefitRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toMap));
    }

    public List<Map<String, Object>> listAll() {
        return employeeBenefitRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current()).stream().map(this::toMap).toList();
    }

    public Map<String, Object> getById(UUID id) {
        return toMap(employeeBenefitRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("EmployeeBenefit not found: " + id)));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> req) {
        EmployeeBenefit e = new EmployeeBenefit();
        e.setTenantId(tenantContext.current());
        e.setName((String) req.get("name"));
        e.setCode((String) req.get("code"));
        e.setType((String) req.get("type"));
        e.setDescription((String) req.get("description"));
        if (req.get("amount") != null) e.setAmount(new BigDecimal(req.get("amount").toString()));
        e.setPercentage(Boolean.TRUE.equals(req.get("percentage")));
        e.setPercentageOf((String) req.get("percentageOf"));
        e.setTaxable(Boolean.TRUE.equals(req.get("taxable")));
        e.setActive(req.get("active") == null || Boolean.TRUE.equals(req.get("active")));
        return toMap(employeeBenefitRepository.save(e));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> req) {
        EmployeeBenefit e = employeeBenefitRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("EmployeeBenefit not found: " + id));
        e.setName((String) req.get("name"));
        e.setCode((String) req.get("code"));
        e.setType((String) req.get("type"));
        e.setDescription((String) req.get("description"));
        if (req.get("amount") != null) e.setAmount(new BigDecimal(req.get("amount").toString()));
        e.setPercentage(Boolean.TRUE.equals(req.get("percentage")));
        e.setPercentageOf((String) req.get("percentageOf"));
        e.setTaxable(Boolean.TRUE.equals(req.get("taxable")));
        if (req.get("active") != null) e.setActive(Boolean.TRUE.equals(req.get("active")));
        return toMap(employeeBenefitRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        EmployeeBenefit e = employeeBenefitRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("EmployeeBenefit not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        employeeBenefitRepository.save(e);
    }

    private Map<String, Object> toMap(EmployeeBenefit e) {
        return Map.of(
                "id", e.getId(), "name", e.getName() != null ? e.getName() : "",
                "code", e.getCode() != null ? e.getCode() : "",
                "type", e.getType() != null ? e.getType() : "",
                "description", e.getDescription() != null ? e.getDescription() : "",
                "amount", e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO,
                "percentage", e.isPercentage(), "taxable", e.isTaxable(), "active", e.isActive()
        );
    }
}

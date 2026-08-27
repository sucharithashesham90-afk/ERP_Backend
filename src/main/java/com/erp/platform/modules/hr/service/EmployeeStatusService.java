package com.erp.platform.modules.hr.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.entity.EmployeeStatus;
import com.erp.platform.modules.hr.repository.EmployeeStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeStatusService {

    private final EmployeeStatusRepository employeeStatusRepository;
    private final TenantContext tenantContext;

    public PageResponse<Map<String, Object>> list(Pageable pageable) {
        return PageResponse.of(employeeStatusRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toMap));
    }

    public List<Map<String, Object>> listAll() {
        return employeeStatusRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current()).stream().map(this::toMap).toList();
    }

    public Map<String, Object> getById(UUID id) {
        return toMap(employeeStatusRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("EmployeeStatus not found: " + id)));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> req) {
        EmployeeStatus e = new EmployeeStatus();
        e.setTenantId(tenantContext.current());
        e.setName((String) req.get("name"));
        e.setCode((String) req.get("code"));
        e.setDescription((String) req.get("description"));
        e.setCategory((String) req.get("category"));
        e.setActive(req.get("active") == null || Boolean.TRUE.equals(req.get("active")));
        return toMap(employeeStatusRepository.save(e));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> req) {
        EmployeeStatus e = employeeStatusRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("EmployeeStatus not found: " + id));
        e.setName((String) req.get("name"));
        e.setCode((String) req.get("code"));
        e.setDescription((String) req.get("description"));
        e.setCategory((String) req.get("category"));
        if (req.get("active") != null) e.setActive(Boolean.TRUE.equals(req.get("active")));
        return toMap(employeeStatusRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        EmployeeStatus e = employeeStatusRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("EmployeeStatus not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        employeeStatusRepository.save(e);
    }

    private Map<String, Object> toMap(EmployeeStatus e) {
        return Map.of(
                "id", e.getId(), "name", e.getName() != null ? e.getName() : "",
                "code", e.getCode() != null ? e.getCode() : "",
                "description", e.getDescription() != null ? e.getDescription() : "",
                "category", e.getCategory() != null ? e.getCategory() : "",
                "active", e.isActive()
        );
    }
}

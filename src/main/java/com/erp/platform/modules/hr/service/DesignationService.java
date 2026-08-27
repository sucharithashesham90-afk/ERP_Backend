package com.erp.platform.modules.hr.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.entity.Designation;
import com.erp.platform.modules.hr.repository.DesignationRepository;
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
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final TenantContext tenantContext;

    public PageResponse<Map<String, Object>> list(Pageable pageable) {
        return PageResponse.of(designationRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toMap));
    }

    public List<Map<String, Object>> listAll() {
        return designationRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current()).stream().map(this::toMap).toList();
    }

    public Map<String, Object> getById(UUID id) {
        return toMap(designationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Designation not found: " + id)));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> req) {
        Designation e = new Designation();
        e.setTenantId(tenantContext.current());
        e.setName((String) req.get("name"));
        e.setCode((String) req.get("code"));
        e.setDescription((String) req.get("description"));
        if (req.get("departmentId") != null && !((String) req.get("departmentId")).isBlank()) e.setDepartmentId(UUID.fromString((String) req.get("departmentId")));
        e.setActive(req.get("active") == null || Boolean.TRUE.equals(req.get("active")));
        return toMap(designationRepository.save(e));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> req) {
        Designation e = designationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Designation not found: " + id));
        e.setName((String) req.get("name"));
        e.setCode((String) req.get("code"));
        e.setDescription((String) req.get("description"));
        if (req.get("departmentId") != null && !((String) req.get("departmentId")).isBlank()) e.setDepartmentId(UUID.fromString((String) req.get("departmentId")));
        if (req.get("active") != null) e.setActive(Boolean.TRUE.equals(req.get("active")));
        return toMap(designationRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        Designation e = designationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Designation not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        designationRepository.save(e);
    }

    private Map<String, Object> toMap(Designation e) {
        return Map.of(
                "id", e.getId(), "name", e.getName() != null ? e.getName() : "",
                "code", e.getCode() != null ? e.getCode() : "",
                "description", e.getDescription() != null ? e.getDescription() : "",
                "departmentId", e.getDepartmentId() != null ? e.getDepartmentId() : "",
                "active", e.isActive()
        );
    }
}

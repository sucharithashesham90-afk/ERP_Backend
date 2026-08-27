package com.erp.platform.modules.organization.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.organization.dto.CreateDepartmentRequest;
import com.erp.platform.modules.organization.dto.DepartmentDto;
import com.erp.platform.modules.organization.entity.Department;
import com.erp.platform.modules.organization.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TenantContext tenantContext;

    public PageResponse<DepartmentDto> list(UUID companyId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = companyId != null
                ? departmentRepository.findByTenantIdAndCompanyIdAndDeletedAtIsNull(tenantId, companyId, pageable)
                : departmentRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public DepartmentDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public DepartmentDto create(CreateDepartmentRequest request) {
        UUID tenantId = tenantContext.current();
        Department dept = new Department();
        dept.setTenantId(tenantId);
        dept.setCompanyId(request.getCompanyId());
        applyRequest(dept, request);
        dept = departmentRepository.save(dept);
        log.info("Department created: id={}, name={}", dept.getId(), dept.getName());
        return toDto(dept);
    }

    @Transactional
    public DepartmentDto update(UUID id, CreateDepartmentRequest request) {
        Department dept = findOrThrow(id);
        applyRequest(dept, request);
        return toDto(departmentRepository.save(dept));
    }

    @Transactional
    public void delete(UUID id) {
        Department dept = findOrThrow(id);
        dept.setDeletedAt(LocalDateTime.now());
        departmentRepository.save(dept);
        log.info("Department soft-deleted: id={}", id);
    }

    private void applyRequest(Department dept, CreateDepartmentRequest r) {
        dept.setName(r.getName());
        dept.setCode(r.getCode());
        dept.setDescription(r.getDescription());
        dept.setParentId(r.getParentId());
    }

    private DepartmentDto toDto(Department d) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(d.getId());
        dto.setTenantId(d.getTenantId());
        dto.setCompanyId(d.getCompanyId());
        dto.setName(d.getName());
        dto.setCode(d.getCode());
        dto.setDescription(d.getDescription());
        dto.setParentId(d.getParentId());
        dto.setActive(d.isActive());
        dto.setCreatedAt(d.getCreatedAt());
        return dto;
    }

    private Department findOrThrow(UUID id) {
        return departmentRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Department not found: " + id));
    }
}

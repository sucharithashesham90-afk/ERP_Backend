package com.erp.platform.modules.master.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.RoleHierarchyDto;
import com.erp.platform.modules.master.dto.RoleHierarchyRequest;
import com.erp.platform.modules.master.entity.RoleHierarchy;
import com.erp.platform.modules.master.repository.RoleHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleHierarchyService {

    private final RoleHierarchyRepository repository;
    private final TenantContext tenantContext;

    public List<RoleHierarchyDto> list() {
        List<RoleHierarchy> all = repository.findByTenantIdAndDeletedAtIsNullOrderByLevelOrder(tenantContext.current());
        Map<String, RoleHierarchy> byRole = all.stream()
                .collect(Collectors.toMap(RoleHierarchy::getRoleName, Function.identity(), (a, b) -> a));
        return all.stream().map(r -> toDto(r, byRole)).collect(Collectors.toList());
    }

    public RoleHierarchyDto getById(UUID id) {
        RoleHierarchy r = findOrThrow(id);
        Map<String, RoleHierarchy> byRole = repository
                .findByTenantIdAndDeletedAtIsNullOrderByLevelOrder(tenantContext.current())
                .stream().collect(Collectors.toMap(RoleHierarchy::getRoleName, Function.identity(), (a, b) -> a));
        return toDto(r, byRole);
    }

    @Transactional
    public RoleHierarchyDto create(RoleHierarchyRequest req) {
        UUID tenantId = tenantContext.current();
        if (repository.existsByTenantIdAndRoleNameAndDeletedAtIsNull(tenantId, req.getRoleName())) {
            throw AppException.conflict("Role already defined in hierarchy: " + req.getRoleName());
        }
        RoleHierarchy r = new RoleHierarchy();
        r.setTenantId(tenantId);
        applyRequest(r, req);
        return toDto(repository.save(r), Map.of());
    }

    @Transactional
    public RoleHierarchyDto update(UUID id, RoleHierarchyRequest req) {
        RoleHierarchy r = findOrThrow(id);
        if (!r.getRoleName().equals(req.getRoleName())
                && repository.existsByTenantIdAndRoleNameAndDeletedAtIsNull(r.getTenantId(), req.getRoleName())) {
            throw AppException.conflict("Role already defined in hierarchy: " + req.getRoleName());
        }
        applyRequest(r, req);
        return toDto(repository.save(r), Map.of());
    }

    @Transactional
    public void delete(UUID id) {
        RoleHierarchy r = findOrThrow(id);
        r.setDeletedAt(LocalDateTime.now());
        repository.save(r);
    }

    private void applyRequest(RoleHierarchy r, RoleHierarchyRequest req) {
        r.setRoleName(req.getRoleName());
        r.setDisplayName(req.getDisplayName());
        r.setParentRole(req.getParentRole());
        r.setLevelOrder(req.getLevelOrder());
        r.setDescription(req.getDescription());
    }

    private RoleHierarchy findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Role hierarchy entry not found: " + id));
    }

    private RoleHierarchyDto toDto(RoleHierarchy r, Map<String, RoleHierarchy> byRole) {
        RoleHierarchyDto dto = new RoleHierarchyDto();
        dto.setId(r.getId());
        dto.setTenantId(r.getTenantId());
        dto.setRoleName(r.getRoleName());
        dto.setDisplayName(r.getDisplayName());
        dto.setParentRole(r.getParentRole());
        dto.setLevelOrder(r.getLevelOrder());
        dto.setDescription(r.getDescription());
        dto.setCreatedAt(r.getCreatedAt());
        if (r.getParentRole() != null && byRole.containsKey(r.getParentRole())) {
            RoleHierarchy parent = byRole.get(r.getParentRole());
            dto.setParentDisplayName(
                    parent.getDisplayName() != null ? parent.getDisplayName() : parent.getRoleName());
        }
        return dto;
    }
}

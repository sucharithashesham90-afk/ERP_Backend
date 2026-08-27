package com.erp.platform.modules.auth.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.auth.dto.CreateRoleRequest;
import com.erp.platform.modules.auth.dto.RoleDto;
import com.erp.platform.modules.auth.entity.Role;
import com.erp.platform.modules.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final TenantContext tenantContext;

    public List<RoleDto> list() {
        UUID tenantId = tenantContext.current();
        return roleRepository.findAllForTenant(tenantId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public RoleDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public RoleDto create(CreateRoleRequest request) {
        UUID tenantId = tenantContext.current();
        String roleName = request.getName().toUpperCase().replace(" ", "_");
        if (roleRepository.existsByNameAndTenantId(roleName, tenantId)) {
            throw AppException.conflict("Role already exists: " + roleName);
        }

        Role role = new Role();
        role.setTenantId(tenantId);
        role.setName(roleName);
        role.setDescription(request.getDescription());
        role.setSystem(false);
        role.setAllowedModules(request.getAllowedModules() != null ? new HashSet<>(request.getAllowedModules()) : new HashSet<>());
        role.setAllowedScreens(request.getAllowedScreens() != null ? new HashSet<>(request.getAllowedScreens()) : new HashSet<>());

        role = roleRepository.save(role);
        log.info("Custom role created: name={}, tenantId={}", role.getName(), tenantId);
        return toDto(role);
    }

    @Transactional
    public RoleDto update(UUID id, CreateRoleRequest request) {
        Role role = findOrThrow(id);
        if (role.isSystem()) {
            throw AppException.badRequest("System roles cannot be modified");
        }
        role.setDescription(request.getDescription());
        role.setAllowedModules(request.getAllowedModules() != null ? new HashSet<>(request.getAllowedModules()) : new HashSet<>());
        role.setAllowedScreens(request.getAllowedScreens() != null ? new HashSet<>(request.getAllowedScreens()) : new HashSet<>());

        log.info("Custom role updated: id={}", id);
        return toDto(roleRepository.save(role));
    }

    @Transactional
    public void delete(UUID id) {
        Role role = findOrThrow(id);
        if (role.isSystem()) {
            throw AppException.badRequest("System roles cannot be deleted");
        }
        roleRepository.delete(role);
        log.info("Custom role deleted: id={}", id);
    }

    private Role findOrThrow(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Role not found: " + id));
    }

    private RoleDto toDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setSystem(role.isSystem());
        dto.setTenantId(role.getTenantId());
        dto.setAllowedModules(role.getAllowedModules());
        dto.setAllowedScreens(role.getAllowedScreens());
        return dto;
    }
}

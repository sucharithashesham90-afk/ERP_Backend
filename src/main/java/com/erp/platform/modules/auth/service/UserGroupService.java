package com.erp.platform.modules.auth.service;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.auth.dto.UserGroupDto;
import com.erp.platform.modules.auth.dto.UserGroupRequest;
import com.erp.platform.modules.auth.entity.UserGroup;
import com.erp.platform.modules.auth.repository.UserGroupRepository;
import com.erp.platform.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGroupService {

    private final UserGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final TenantContext tenantContext;

    public List<UserGroupDto> listAll() {
        UUID tenantId = tenantContext.current();
        return groupRepository.findByTenantIdAndDeletedAtIsNull(tenantId)
                .stream()
                .map(g -> toDto(g, userRepository.countByGroupId(g.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserGroupDto create(UserGroupRequest req) {
        UUID tenantId = tenantContext.current();
        if (groupRepository.existsByTenantIdAndNameAndDeletedAtIsNull(tenantId, req.getName())) {
            throw AppException.conflict("Group already exists: " + req.getName());
        }
        UserGroup group = new UserGroup();
        group.setTenantId(tenantId);
        group.setName(req.getName());
        group.setDescription(req.getDescription());
        group.setGroupLeader(req.getGroupLeader());
        group.setGroupEmail(req.getGroupEmail());
        group.setHierarchyLevel(req.getHierarchyLevel());
        group.setAllowedRoles(serializeRoles(req.getAllowedRoles()));
        return toDto(groupRepository.save(group), 0);
    }

    @Transactional
    public UserGroupDto update(UUID id, UserGroupRequest req) {
        UUID tenantId = tenantContext.current();
        UserGroup group = groupRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Group not found: " + id));
        group.setName(req.getName());
        group.setDescription(req.getDescription());
        group.setGroupLeader(req.getGroupLeader());
        group.setGroupEmail(req.getGroupEmail());
        group.setHierarchyLevel(req.getHierarchyLevel());
        group.setAllowedRoles(serializeRoles(req.getAllowedRoles()));
        return toDto(groupRepository.save(group), userRepository.countByGroupId(id));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        UserGroup group = groupRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Group not found: " + id));
        group.setDeletedAt(LocalDateTime.now());
        groupRepository.save(group);
    }

    public UserGroupDto toDto(UserGroup g, long memberCount) {
        UserGroupDto dto = new UserGroupDto();
        dto.setId(g.getId());
        dto.setName(g.getName());
        dto.setDescription(g.getDescription());
        dto.setGroupLeader(g.getGroupLeader());
        dto.setGroupEmail(g.getGroupEmail());
        dto.setHierarchyLevel(g.getHierarchyLevel());
        dto.setAllowedRoles(deserializeRoles(g.getAllowedRoles()));
        dto.setMemberCount(memberCount);
        return dto;
    }

    private String serializeRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) return "";
        return String.join(",", roles);
    }

    private List<String> deserializeRoles(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.asList(raw.split(","));
    }
}

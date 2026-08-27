package com.erp.platform.modules.auth.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.auth.dto.CreateRoleRequest;
import com.erp.platform.modules.auth.dto.RoleDto;
import com.erp.platform.modules.auth.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/settings/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ApiResponse<List<RoleDto>> list() {
        return ApiResponse.success(roleService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleDto> getById(@PathVariable UUID id) {
        return ApiResponse.success(roleService.getById(id));
    }

    @PostMapping
    public ApiResponse<RoleDto> create(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(roleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleDto> update(@PathVariable UUID id, @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

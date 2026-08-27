package com.erp.platform.modules.organization.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.organization.dto.CompanyDto;
import com.erp.platform.modules.organization.dto.CreateCompanyRequest;
import com.erp.platform.modules.organization.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/org/company")
@RequiredArgsConstructor
@Tag(name = "Organization - Company", description = "Company management")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List companies")
    public ResponseEntity<ApiResponse<PageResponse<CompanyDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(companyService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get company by ID")
    public ResponseEntity<ApiResponse<CompanyDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a company")
    public ResponseEntity<ApiResponse<CompanyDto>> create(@Valid @RequestBody CreateCompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(companyService.create(request), "Company created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update company")
    public ResponseEntity<ApiResponse<CompanyDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCompanyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(companyService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete company (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        companyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Company deleted successfully"));
    }
}

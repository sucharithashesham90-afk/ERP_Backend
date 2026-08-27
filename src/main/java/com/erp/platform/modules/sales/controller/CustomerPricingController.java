package com.erp.platform.modules.sales.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.entity.CustomerPricing;
import com.erp.platform.modules.sales.repository.CustomerPricingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales/customer-pricing")
@RequiredArgsConstructor
@Tag(name = "Sales - Customer Pricing", description = "Customer-specific pricing details")
public class CustomerPricingController {

    private final CustomerPricingRepository customerPricingRepository;
    private final TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List customer pricing details")
    public ResponseEntity<ApiResponse<List<CustomerPricing>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                customerPricingRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get customer pricing by ID")
    public ResponseEntity<ApiResponse<CustomerPricing>> getById(@PathVariable UUID id) {
        CustomerPricing entity = customerPricingRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Customer pricing not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(entity));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create customer pricing")
    public ResponseEntity<ApiResponse<CustomerPricing>> create(@RequestBody CustomerPricing req) {
        req.setTenantId(tenantContext.current());
        return ResponseEntity.ok(ApiResponse.success(customerPricingRepository.save(req), "Created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update customer pricing")
    public ResponseEntity<ApiResponse<CustomerPricing>> update(@PathVariable UUID id, @RequestBody CustomerPricing req) {
        CustomerPricing e = customerPricingRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Customer pricing not found: " + id));
        e.setCustomerId(req.getCustomerId());
        e.setCustomerName(req.getCustomerName());
        e.setProductId(req.getProductId());
        e.setProductName(req.getProductName());
        e.setCropGroupId(req.getCropGroupId());
        e.setCropGroupName(req.getCropGroupName());
        e.setCropId(req.getCropId());
        e.setCropName(req.getCropName());
        e.setVarietyId(req.getVarietyId());
        e.setVarietyName(req.getVarietyName());
        e.setPrice(req.getPrice());
        e.setEffectiveDate(req.getEffectiveDate());
        e.setExpiryDate(req.getExpiryDate());
        e.setActive(req.isActive());
        return ResponseEntity.ok(ApiResponse.success(customerPricingRepository.save(e), "Updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Delete customer pricing")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        CustomerPricing e = customerPricingRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Customer pricing not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        customerPricingRepository.save(e);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}

package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.dto.CreateCustomerRequest;
import com.erp.platform.modules.master.dto.CustomerDto;
import com.erp.platform.modules.master.dto.UpdateCustomerRequest;
import com.erp.platform.modules.master.service.CustomerService;
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
@RequestMapping("/api/v1/master/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer master data management")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List customers with pagination and search")
    public ResponseEntity<ApiResponse<PageResponse<CustomerDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        return ResponseEntity.ok(ApiResponse.success(customerService.list(search, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new customer")
    public ResponseEntity<ApiResponse<CustomerDto>> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(customerService.create(request), "Customer created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update customer")
    public ResponseEntity<ApiResponse<CustomerDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete customer (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer deleted successfully"));
    }
}

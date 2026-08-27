package com.erp.platform.modules.dispatch.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.dispatch.dto.CreateDispatchChallanRequest;
import com.erp.platform.modules.dispatch.dto.DispatchChallanDto;
import com.erp.platform.modules.dispatch.service.DispatchChallanService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dispatch/dispatch-challans")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DispatchChallanController {

    private final DispatchChallanService dispatchChallanService;
    private final com.erp.platform.modules.dispatch.service.DispatchPostingService dispatchPostingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DispatchChallanDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(dispatchChallanService.list(pageable)));
    }

    /**
     * Challans dispatched to one customer, lines included. The Dealer-to-Dealer transfer screen
     * calls this once the "from" dealer is picked so that only stock that actually reached that
     * dealer can be transferred on.
     */
    @GetMapping("/by-customer")
    public ResponseEntity<ApiResponse<java.util.List<DispatchChallanDto>>> byCustomer(
            @RequestParam String customer) {
        return ResponseEntity.ok(ApiResponse.success(dispatchChallanService.listByCustomer(customer)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DispatchChallanDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(dispatchChallanService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DispatchChallanDto>> create(@Valid @RequestBody CreateDispatchChallanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dispatchChallanService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DispatchChallanDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDispatchChallanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dispatchChallanService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        dispatchChallanService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/dispatch")
    @Operation(summary = "Dispatch the challan: reduce lot stock and auto-create the sales invoice")
    public ResponseEntity<ApiResponse<DispatchChallanDto>> dispatch(@PathVariable UUID id) {
        dispatchPostingService.dispatch(id);
        return ResponseEntity.ok(ApiResponse.success(dispatchChallanService.getById(id),
                "Dispatched: stock reduced and sales invoice created"));
    }
}

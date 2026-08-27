package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.service.CreditNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/credit-notes")
@RequiredArgsConstructor
@Tag(name = "Accounting - Credit Notes", description = "Credit note management")
public class CreditNoteController {

    private final CreditNoteService creditNoteService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List credit notes")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(creditNoteService.list(type, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get credit note by ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(creditNoteService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create credit note")
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(creditNoteService.create(request)));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Post credit note to ledger")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(creditNoteService.post(id), "Credit note posted to ledger"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT', 'ACCOUNT_MANAGER')")
    @Operation(summary = "Delete DRAFT credit note")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        creditNoteService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

package com.erp.platform.modules.accounting.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.dto.CreateJournalEntryRequest;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntry.JEStatus;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounting/journal-entries")
@RequiredArgsConstructor
@Tag(name = "Accounting - Journal Entries", description = "Journal entry management")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List journal entries")
    public ResponseEntity<ApiResponse<PageResponse<JournalEntry>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) JEStatus status) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "entryDate"));
        return ResponseEntity.ok(ApiResponse.success(journalEntryService.list(status, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get journal entry by ID")
    public ResponseEntity<ApiResponse<JournalEntry>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(journalEntryService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create journal entry")
    public ResponseEntity<ApiResponse<JournalEntry>> create(@RequestBody CreateJournalEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(journalEntryService.create(request), "Journal entry created"));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Post journal entry")
    public ResponseEntity<ApiResponse<JournalEntry>> post(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(journalEntryService.post(id), "Entry posted"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel journal entry")
    public ResponseEntity<ApiResponse<JournalEntry>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(journalEntryService.cancel(id), "Entry cancelled"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete journal entry (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        journalEntryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Journal entry deleted successfully"));
    }
}

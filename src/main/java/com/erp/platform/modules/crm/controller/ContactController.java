package com.erp.platform.modules.crm.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.crm.entity.Contact;
import com.erp.platform.modules.crm.service.ContactService;
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
@RequestMapping("/api/v1/crm/contacts")
@RequiredArgsConstructor
@Tag(name = "CRM - Contacts", description = "Contact management")
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List contacts")
    public ResponseEntity<ApiResponse<PageResponse<Contact>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(contactService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get contact by ID")
    public ResponseEntity<ApiResponse<Contact>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(contactService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create contact")
    public ResponseEntity<ApiResponse<Contact>> create(@RequestBody Contact contact) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(contactService.create(contact), "Contact created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update contact")
    public ResponseEntity<ApiResponse<Contact>> update(@PathVariable UUID id, @RequestBody Contact body) {
        return ResponseEntity.ok(ApiResponse.success(contactService.update(id, body)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete contact (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        contactService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Contact deleted successfully"));
    }
}

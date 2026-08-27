package com.erp.platform.modules.promotions.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.promotions.entity.MarketingCampaign;
import com.erp.platform.modules.promotions.service.MarketingCampaignService;
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
@RequestMapping("/api/v1/promotions/campaigns")
@RequiredArgsConstructor
@Tag(name = "Marketing - Omni-Channel Campaigns")
public class MarketingCampaignController {

    private final MarketingCampaignService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List marketing campaigns")
    public ResponseEntity<ApiResponse<PageResponse<MarketingCampaign>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(service.list(pageable))));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create (save) a campaign")
    public ResponseEntity<ApiResponse<MarketingCampaign>> create(@RequestBody Map<String, Object> req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req), "Campaign saved"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a campaign")
    public ResponseEntity<ApiResponse<MarketingCampaign>> update(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, req), "Campaign updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a campaign")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Campaign deleted"));
    }

    @PostMapping("/{id}/send-test")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a test message to a single email")
    public ResponseEntity<ApiResponse<Boolean>> sendTest(@PathVariable UUID id, @RequestBody Map<String, Object> req) {
        Object email = req.get("email");
        boolean sent = service.sendTest(id, email == null ? null : email.toString());
        return ResponseEntity.ok(ApiResponse.success(sent, sent ? "Test message sent" : "Test message could not be sent"));
    }

    @PostMapping("/{id}/launch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Launch the campaign to all recipients")
    public ResponseEntity<ApiResponse<Map<String, Object>>> launch(@PathVariable UUID id) {
        var result = service.launch(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Campaign launched"));
    }
}

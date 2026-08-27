package com.erp.platform.modules.purchase.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.purchase.dto.CreatePaymentLiabilityRequest;
import com.erp.platform.modules.purchase.dto.PaymentLiabilityDto;
import com.erp.platform.modules.purchase.service.PaymentLiabilityService;
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
@RequestMapping("/api/v1/purchase/payment-liabilities")
@RequiredArgsConstructor
@Tag(name = "Payment Liabilities", description = "Manage purchase payment liabilities")
public class PaymentLiabilityController {

    private final PaymentLiabilityService service;
    private final com.erp.platform.modules.purchase.service.LiabilityPaymentService liabilityPaymentService;
    private final com.erp.platform.modules.purchase.service.IntakeLiabilityService intakeLiabilityService;
    private final com.erp.platform.modules.intake.repository.IntakeSlipRepository intakeSlipRepository;
    private final com.erp.platform.common.tenant.TenantContext tenantContext;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List payment liabilities")
    public ResponseEntity<ApiResponse<PageResponse<PaymentLiabilityDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("liabilityNumber"));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    /**
     * The Liability Payment worklist: unsettled liabilities matching the chosen filters.
     *
     * @param partyType GROWER, ORGANIZER, or blank for both
     * @param partyName one specific grower/organizer, or blank for all of that type
     * @param intake    LOT (lot-wise intake), TRUCK (truck-wise), or blank for both
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search unsettled liabilities to post payments against")
    public ResponseEntity<ApiResponse<java.util.List<PaymentLiabilityDto>>> search(
            @RequestParam(required = false, defaultValue = "") String partyType,
            @RequestParam(required = false, defaultValue = "") String partyName,
            @RequestParam(required = false, defaultValue = "") String intake,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(ApiResponse.success(
                service.searchForPosting(partyType, partyName, intake, parseDate(from), parseDate(to))));
    }

    /** Grower / organizer names that actually have liabilities, for the picker. */
    @GetMapping("/parties")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Distinct grower/organizer names holding liabilities")
    public ResponseEntity<ApiResponse<java.util.List<String>>> parties(
            @RequestParam(required = false, defaultValue = "") String partyType) {
        return ResponseEntity.ok(ApiResponse.success(service.partyNames(partyType)));
    }

    /**
     * Post payment for every selected liability in one go.
     *
     * <p>Each row settles its own outstanding balance through the same path as the single-row pay,
     * so each still gets its own voucher on the party's ledger. One failure does not abandon the
     * rest — the response reports per-row outcomes so a partial run is visible rather than silent.
     */
    @PostMapping("/post-batch")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Post payments for the selected liabilities")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> postBatch(
            @RequestBody java.util.Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<String> ids = body.get("ids") instanceof java.util.List
                ? (java.util.List<String>) body.get("ids") : java.util.List.of();
        if (ids.isEmpty()) {
            throw com.erp.platform.common.exception.AppException.badRequest("Select at least one liability to post");
        }
        String paymentMode = body.get("paymentMode") == null ? null : String.valueOf(body.get("paymentMode"));
        String reference   = body.get("reference") == null ? null : String.valueOf(body.get("reference"));
        java.time.LocalDate date = parseDate(body.get("paymentDate") == null ? null : String.valueOf(body.get("paymentDate")));

        java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();
        int posted = 0;
        for (String rawId : ids) {
            java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("id", rawId);
            try {
                liabilityPaymentService.pay(UUID.fromString(rawId), null, paymentMode, reference, date);
                row.put("status", "POSTED");
                posted++;
            } catch (Exception e) {
                row.put("status", "FAILED");
                row.put("error", e.getMessage());
            }
            results.add(row);
        }

        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("requested", ids.size());
        out.put("posted", posted);
        out.put("failed", ids.size() - posted);
        out.put("results", results);
        return ResponseEntity.ok(ApiResponse.success(out,
                posted + " of " + ids.size() + " liabilities posted"));
    }

    private static java.time.LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return java.time.LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentLiabilityDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    /**
     * Pay a liability and post the voucher for it.
     *
     * <p>Omit the amount to settle the whole outstanding balance. The entry it writes carries the
     * liability number, so it turns up in Voucher Search and on the grower's or organizer's ledger.
     */
    /**
     * Raise liabilities for intakes that were completed before intakes did so themselves.
     *
     * <p>Nothing created a liability until now, so every intake already on file left the grower
     * owed with no record of it. This walks the completed ones and raises what is missing. Keyed on
     * the slip number like the live path, so it is safe to run twice and cannot double-claim.
     */
    @PostMapping("/backfill-from-intakes")
    @PreAuthorize("hasAnyRole('ADMIN','TENANT_ADMIN')")
    @Operation(summary = "Raise liabilities for already-completed intakes")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> backfillFromIntakes() {
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        int raised = 0, skipped = 0, failed = 0;

        var slips = intakeSlipRepository.findByTenantIdAndDeletedAtIsNull(
                tenantContext.current(), org.springframework.data.domain.PageRequest.of(0, 20000));

        for (var slip : slips) {
            if (slip.getStatus() != com.erp.platform.modules.intake.entity.IntakeSlip.SlipStatus.COMPLETED) {
                skipped++;
                continue;
            }
            try {
                boolean already = intakeLiabilityService.exists(
                        slip.getTenantId(), "LIA-" + slip.getSlipNumber());
                intakeLiabilityService.raiseFor(slip);
                if (already) skipped++; else raised++;
            } catch (Exception e) {
                failed++;
            }
        }

        out.put("intakesExamined", slips.getTotalElements());
        out.put("liabilitiesRaised", raised);
        out.put("alreadyPresentOrNotCompleted", skipped);
        out.put("failed", failed);
        return ResponseEntity.ok(ApiResponse.success(out,
                raised == 0 ? "Nothing to raise - no completed intakes without a liability."
                            : raised + " liabilit" + (raised == 1 ? "y" : "ies") + " raised"));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Pay a grower/organizer liability and post the voucher")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> pay(
            @PathVariable UUID id,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        java.util.Map<String, Object> b = body != null ? body : java.util.Map.of();
        java.math.BigDecimal amount = null;
        Object amt = b.get("amount");
        if (amt != null && !String.valueOf(amt).isBlank()) {
            try { amount = new java.math.BigDecimal(String.valueOf(amt).trim()); } catch (NumberFormatException ignored) { }
        }
        java.time.LocalDate date = null;
        Object d = b.get("paymentDate");
        if (d != null && !String.valueOf(d).isBlank()) {
            try { date = java.time.LocalDate.parse(String.valueOf(d)); } catch (Exception ignored) { }
        }
        return ResponseEntity.ok(ApiResponse.success(
                liabilityPaymentService.pay(id, amount,
                        b.get("paymentMode") == null ? null : String.valueOf(b.get("paymentMode")),
                        b.get("reference") == null ? null : String.valueOf(b.get("reference")),
                        date),
                "Liability paid and voucher posted"));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentLiabilityDto>> create(@Valid @RequestBody CreatePaymentLiabilityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(request), "paymentLiability created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaymentLiabilityDto>> update(@PathVariable UUID id, @Valid @RequestBody CreatePaymentLiabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "paymentLiability deleted"));
    }
}

package com.erp.platform.modules.quality.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.entity.CropVarietyTest;
import com.erp.platform.modules.agri.repository.CropVarietyTestRepository;
import com.erp.platform.modules.inventory.entity.InventoryIssue;
import com.erp.platform.modules.inventory.entity.StockLot;
import com.erp.platform.modules.inventory.repository.InventoryIssueRepository;
import com.erp.platform.modules.inventory.repository.StockLotRepository;
import com.erp.platform.modules.quality.dto.CreateSampleRequest;
import com.erp.platform.modules.quality.dto.SampleDto;
import com.erp.platform.modules.quality.entity.Sample;
import com.erp.platform.modules.quality.entity.Sample.SampleStatus;
import com.erp.platform.modules.quality.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SampleService {

    private final SampleRepository sampleRepository;
    private final TenantContext tenantContext;
    private final CropVarietyTestRepository cropVarietyTestRepository;
    private final StockLotRepository stockLotRepository;
    private final InventoryIssueRepository inventoryIssueRepository;

    public PageResponse<SampleDto> list(SampleStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? sampleRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : sampleRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    /**
     * Lot Search rows, built from samples that carry a recorded result.
     *
     * <p>Shaped to what the Lot Search screen renders rather than to SampleDto, so the screen needs
     * no knowledge of where results happen to be stored.
     */
    public PageResponse<java.util.Map<String, Object>> searchResults(String lot, String product,
            java.time.LocalDate from, java.time.LocalDate to, String result, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(sampleRepository.searchResults(tenantId,
                lot == null ? "" : lot.trim(),
                product == null ? "" : product.trim(),
                from, to,
                result == null ? "" : result.trim(),
                pageable).map(this::toSearchRow));
    }

    private java.util.Map<String, Object> toSearchRow(Sample s) {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", s.getId());
        // The grid reads `lotNo`, not `lotNumber` — emit the name the screen actually binds to.
        m.put("lotNo", s.getLotNumber() == null ? "" : s.getLotNumber());
        String product = s.getProductName();
        if (product == null || product.isBlank()) {
            product = java.util.stream.Stream.of(s.getCropName(), s.getVarietyName())
                    .filter(v -> v != null && !v.isBlank())
                    .collect(Collectors.joining(" / "));
        }
        m.put("product", product);
        m.put("testDate", s.getSampleDate());
        m.put("testName", s.getMaterialGroupName() == null || s.getMaterialGroupName().isBlank()
                ? "Result Entry" : s.getMaterialGroupName());
        m.put("result", s.getResultStatus() == null || s.getResultStatus().isBlank()
                ? "PENDING" : s.getResultStatus());
        m.put("tester", s.getCollectedBy() == null ? "" : s.getCollectedBy());
        m.put("remarks", s.getSampleNumber() == null ? "" : s.getSampleNumber());
        m.put("parameters", parseParameters(s.getResultsJson()));
        m.put("score", averageNumeric(s.getResultsJson()));
        return m;
    }

    /** Result Entry stores a flat {"propertyName": "value"} object; expand it for the detail view. */
    private java.util.List<java.util.Map<String, Object>> parseParameters(String resultsJson) {
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        if (resultsJson == null || resultsJson.isBlank()) return out;
        try {
            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(resultsJson);
            node.fields().forEachRemaining(e -> {
                java.util.Map<String, Object> p = new java.util.LinkedHashMap<>();
                p.put("paramName", e.getKey());
                p.put("expectedValue", "");
                p.put("actualValue", e.getValue().asText());
                p.put("unit", "");
                p.put("result", "");
                out.add(p);
            });
        } catch (Exception ignored) {
            // A malformed blob should not sink the whole search result.
        }
        return out;
    }

    /** Mean of whatever numeric readings were captured, so the list can show a single score. */
    private Double averageNumeric(String resultsJson) {
        java.util.List<java.util.Map<String, Object>> params = parseParameters(resultsJson);
        java.util.List<Double> nums = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> p : params) {
            try { nums.add(Double.parseDouble(String.valueOf(p.get("actualValue")).trim())); }
            catch (NumberFormatException ignored) { /* non-numeric readings simply do not score */ }
        }
        return nums.isEmpty() ? null : nums.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public SampleDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public SampleDto create(CreateSampleRequest request) {
        UUID tenantId = tenantContext.current();
        Sample sample = new Sample();
        sample.setTenantId(tenantId);
        sample.setSampleNumber(generateSampleNumber(tenantId));
        sample.setProductId(request.getProductId());
        sample.setProductName(request.getProductName());
        sample.setSourceType(request.getSourceType());
        sample.setSourceId(request.getSourceId());
        sample.setSourceReference(request.getSourceReference());
        sample.setLotNumber(request.getLotNumber());
        sample.setSampleDate(request.getSampleDate() != null ? request.getSampleDate() : LocalDate.now());
        sample.setSampleSize(request.getSampleSize());
        sample.setUnit(request.getUnit());
        sample.setCollectedBy(request.getCollectedBy());
        sample.setStatus(SampleStatus.COLLECTED);
        sample.setWarehouseId(request.getWarehouseId());
        applySeedClassification(sample, request);
        sample.setCropVarietyTestId(request.getCropVarietyTestId());
        sample.setNotes(request.getNotes());

        Sample saved = sampleRepository.save(sample);
        applyInventoryDeduction(saved);
        log.info("Sample created: id={}, number={}", saved.getId(), saved.getSampleNumber());
        return toDto(saved);
    }

    /**
     * If the sample's Crop/Variety Test has "Update inventory for sample quantity" enabled, deduct
     * the configured sample quantity from the matching stock lot and record an inventory issue.
     */
    private void applyInventoryDeduction(Sample sample) {
        UUID tenantId = sample.getTenantId();
        UUID cvtId = sample.getCropVarietyTestId();
        if (cvtId == null) return;
        CropVarietyTest cvt = cropVarietyTestRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, cvtId).orElse(null);
        if (cvt == null || !cvt.isUpdateInventory()) return;
        BigDecimal qty = cvt.getSampleQuantity();
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) return;
        if (sample.getLotNumber() == null || sample.getLotNumber().isBlank()) {
            log.warn("Sample {} has updateInventory test but no lot number — skipping deduction", sample.getSampleNumber());
            return;
        }

        StockLot lot = stockLotRepository
                .findFirstByTenantIdAndLotNoAndDeletedAtIsNullOrderByCreatedAtAsc(tenantId, sample.getLotNumber())
                .orElse(null);
        if (lot == null) {
            log.warn("No stock lot found for lot {} — skipping sample-quantity deduction", sample.getLotNumber());
            return;
        }
        BigDecimal available = lot.getQuantity() != null ? lot.getQuantity() : BigDecimal.ZERO;
        BigDecimal deduct = qty.min(available);
        lot.setQuantity(available.subtract(deduct));
        stockLotRepository.save(lot);

        InventoryIssue issue = new InventoryIssue();
        issue.setTenantId(tenantId);
        issue.setIssueNumber("ISS-" + sample.getSampleNumber());
        issue.setGodownId(lot.getGodownId());
        issue.setGodownName(lot.getGodownName());
        issue.setNetId(lot.getNetId());
        issue.setIssueDate(LocalDate.now());
        issue.setIssuedBy(sample.getCollectedBy());
        issue.setIssueTo("Quality sample " + sample.getSampleNumber());
        issue.setNotes("Auto-issue of " + deduct + " " + (cvt.getSampleQuantityUom() != null ? cvt.getSampleQuantityUom() : "")
                + " from lot " + sample.getLotNumber() + " for quality testing");
        inventoryIssueRepository.save(issue);
        log.info("Deducted {} from lot {} and created issue {} for sample {}",
                deduct, sample.getLotNumber(), issue.getIssueNumber(), sample.getSampleNumber());
    }

    /**
     * Assign a single shared batch number to all the given samples (Sample Submission "Create").
     */
    @Transactional
    public String createBatch(List<UUID> sampleIds) {
        UUID tenantId = tenantContext.current();
        if (sampleIds == null || sampleIds.isEmpty()) throw AppException.badRequest("Select at least one sample");
        String batchNumber = generateBatchNumber(tenantId);
        for (UUID id : sampleIds) {
            Sample s = sampleRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id).orElse(null);
            if (s == null) continue;
            s.setBatchNumber(batchNumber);
            s.setStatus(SampleStatus.SUBMITTED);
            sampleRepository.save(s);
        }
        log.info("Created batch {} for {} samples", batchNumber, sampleIds.size());
        return batchNumber;
    }

    @Transactional
    public SampleDto update(UUID id, CreateSampleRequest request) {
        Sample sample = findOrThrow(id);
        sample.setProductId(request.getProductId());
        sample.setProductName(request.getProductName());
        sample.setSourceType(request.getSourceType());
        sample.setSourceId(request.getSourceId());
        sample.setSourceReference(request.getSourceReference());
        sample.setLotNumber(request.getLotNumber());
        sample.setSampleDate(request.getSampleDate());
        sample.setSampleSize(request.getSampleSize());
        sample.setUnit(request.getUnit());
        sample.setCollectedBy(request.getCollectedBy());
        sample.setWarehouseId(request.getWarehouseId());
        applySeedClassification(sample, request);
        sample.setCropVarietyTestId(request.getCropVarietyTestId());
        sample.setNotes(request.getNotes());
        return toDto(sampleRepository.save(sample));
    }

    private void applySeedClassification(Sample s, CreateSampleRequest r) {
        s.setCropGroupId(r.getCropGroupId());
        s.setCropGroupName(r.getCropGroupName());
        s.setCropId(r.getCropId());
        s.setCropName(r.getCropName());
        s.setVarietyId(r.getVarietyId());
        s.setVarietyName(r.getVarietyName());
        s.setSeedStateId(r.getSeedStateId());
        s.setSeedStateName(r.getSeedStateName());
        s.setLocation(r.getLocation());
    }

    @Transactional
    public SampleDto updateStatus(UUID id, SampleStatus status) {
        Sample sample = findOrThrow(id);
        sample.setStatus(status);
        return toDto(sampleRepository.save(sample));
    }

    @Transactional
    public void delete(UUID id) {
        Sample sample = findOrThrow(id);
        sample.setDeletedAt(LocalDateTime.now());
        sampleRepository.save(sample);
        log.info("Sample soft-deleted: id={}", id);
    }

    public SampleDto toDto(Sample s) {
        SampleDto dto = new SampleDto();
        dto.setId(s.getId());
        dto.setTenantId(s.getTenantId());
        dto.setSampleNumber(s.getSampleNumber());
        dto.setProductId(s.getProductId());
        dto.setProductName(s.getProductName());
        dto.setSourceType(s.getSourceType());
        dto.setSourceId(s.getSourceId());
        dto.setSourceReference(s.getSourceReference());
        dto.setLotNumber(s.getLotNumber());
        dto.setSampleDate(s.getSampleDate());
        dto.setSampleSize(s.getSampleSize());
        dto.setUnit(s.getUnit());
        dto.setCollectedBy(s.getCollectedBy());
        dto.setStatus(s.getStatus());
        dto.setWarehouseId(s.getWarehouseId());
        dto.setCropGroupId(s.getCropGroupId());
        dto.setCropGroupName(s.getCropGroupName());
        dto.setCropId(s.getCropId());
        dto.setCropName(s.getCropName());
        dto.setVarietyId(s.getVarietyId());
        dto.setVarietyName(s.getVarietyName());
        dto.setSeedStateId(s.getSeedStateId());
        dto.setSeedStateName(s.getSeedStateName());
        dto.setLocation(s.getLocation());
        dto.setCropVarietyTestId(s.getCropVarietyTestId());
        dto.setBatchNumber(s.getBatchNumber());
        dto.setResultStatus(s.getResultStatus());
        dto.setResultsJson(s.getResultsJson());
        dto.setNotes(s.getNotes());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }

    /** Persist Result Entry outcome (observed values + PASS/FAIL) against the sample. */
    @Transactional
    public SampleDto saveResult(UUID id, String resultStatus, String resultsJson) {
        Sample sample = findOrThrow(id);
        sample.setResultStatus(resultStatus);
        sample.setResultsJson(resultsJson);
        if ("PASS".equalsIgnoreCase(resultStatus) || "FAIL".equalsIgnoreCase(resultStatus)) {
            sample.setStatus(SampleStatus.COMPLETED);
        }
        return toDto(sampleRepository.save(sample));
    }

    private Sample findOrThrow(UUID id) {
        return sampleRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Sample not found: " + id));
    }

    private String generateSampleNumber(UUID tenantId) {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = sampleRepository.countByTenantIdAndDeletedAtIsNull(tenantId) + 1;
        return String.format("SAMP-%s-%03d", year, count);
    }

    private String generateBatchNumber(UUID tenantId) {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = sampleRepository.countByTenantIdAndDeletedAtIsNull(tenantId) + 1;
        return String.format("BATCH-%s-%04d", year, count);
    }
}

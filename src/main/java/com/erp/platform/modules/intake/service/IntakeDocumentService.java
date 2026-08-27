package com.erp.platform.modules.intake.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.util.PayloadUtils;
import com.erp.platform.modules.intake.entity.IntakeDocument;
import com.erp.platform.modules.intake.repository.IntakeDocumentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Shared persistence for JSON-backed intake documents. */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IntakeDocumentService {

    private final IntakeDocumentRepository repo;
    private final com.erp.platform.modules.inventory.repository.StockLotRepository stockLotRepository;
    private final TenantContext tenantContext;
    private final ObjectMapper objectMapper;

    public PageResponse<Map<String, Object>> list(String type, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(
                repo.findByTenantIdAndTypeAndDeletedAtIsNull(tenantId, type, pageable).map(this::toMap));
    }

    @Transactional
    public Map<String, Object> create(String type, Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        IntakeDocument e = new IntakeDocument();
        e.setTenantId(tenantId);
        e.setType(type);
        apply(e, body);
        IntakeDocument saved = repo.save(e);
        if ("SALES_RETURN".equalsIgnoreCase(type) || "SALES_RETURN_INTAKE".equalsIgnoreCase(type)) {
            processSalesReturnInventoryLots(tenantId, saved, body);
        }
        return toMap(saved);
    }

    @Transactional
    public Map<String, Object> update(String type, UUID id, Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        IntakeDocument e = repo.findByTenantIdAndIdAndTypeAndDeletedAtIsNull(tenantId, id, type)
                .orElseThrow(() -> AppException.notFound("Intake not found: " + id));
        apply(e, body);
        return toMap(repo.save(e));
    }

    @Transactional
    public void delete(String type, UUID id) {
        UUID tenantId = tenantContext.current();
        IntakeDocument e = repo.findByTenantIdAndIdAndTypeAndDeletedAtIsNull(tenantId, id, type)
                .orElseThrow(() -> AppException.notFound("Intake not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        repo.save(e);
    }

    private void apply(IntakeDocument e, Map<String, Object> body) {
        if (body == null) body = Map.of();
        String intakeSlip = PayloadUtils.str(body, "intakeSlip");
        LocalDate intakeDate = PayloadUtils.date(body, "intakeDate");
        e.setIntakeSlip(intakeSlip);
        e.setIntakeDate(intakeDate != null ? intakeDate : LocalDate.now());
        try {
            e.setPayloadJson(objectMapper.writeValueAsString(body));
        } catch (Exception ex) {
            throw AppException.badRequest("Invalid intake payload: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(IntakeDocument e) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (e.getPayloadJson() != null) {
            try {
                m.putAll(objectMapper.readValue(e.getPayloadJson(), new TypeReference<Map<String, Object>>() {}));
            } catch (Exception ex) {
                log.warn("Failed to parse intake payload {}: {}", e.getId(), ex.getMessage());
            }
        }
        m.put("id", e.getId());
        m.put("intakeSlip", e.getIntakeSlip());
        m.put("intakeDate", e.getIntakeDate() == null ? null : e.getIntakeDate().toString());
        m.put("createdAt", e.getCreatedAt() == null ? null : e.getCreatedAt().toString());
        return m;
    }

    @SuppressWarnings("unchecked")
    private void processSalesReturnInventoryLots(UUID tenantId, IntakeDocument doc, Map<String, Object> body) {
        if (body == null) return;
        Object linesObj = body.get("lines");
        if (!(linesObj instanceof java.util.List)) return;
        java.util.List<Map<String, Object>> lines = (java.util.List<Map<String, Object>>) linesObj;
        String locationName = PayloadUtils.str(body, "locationName");
        if (locationName == null || locationName.isBlank()) locationName = "Main Warehouse";

        for (Map<String, Object> line : lines) {
            String lotNo = "SRI-" + (System.currentTimeMillis() % 100000) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            String cropGroupName = PayloadUtils.str(line, "cropGroupName");
            if (cropGroupName == null || cropGroupName.isBlank()) cropGroupName = PayloadUtils.str(body, "cropGroupName");
            String cropName = PayloadUtils.str(line, "cropName");
            String varietyName = PayloadUtils.str(line, "varietyName");
            String productName = PayloadUtils.str(line, "productName");
            String godownName = PayloadUtils.str(line, "godownName");
            if (godownName == null || godownName.isBlank()) godownName = locationName;
            String materialState = PayloadUtils.str(line, "materialState");
            if (materialState == null || materialState.isBlank()) materialState = "RAW";
            String materialStateName = PayloadUtils.str(line, "materialStateName");
            if (materialStateName == null || materialStateName.isBlank()) materialStateName = "Returned Raw";
            String qtyStr = PayloadUtils.str(line, "quantity");
            java.math.BigDecimal qty = java.math.BigDecimal.ONE;
            try {
                if (qtyStr != null && !qtyStr.isBlank()) qty = new java.math.BigDecimal(qtyStr);
            } catch (Exception ignored) {}

            com.erp.platform.modules.inventory.entity.StockLot lot = new com.erp.platform.modules.inventory.entity.StockLot();
            lot.setTenantId(tenantId);
            lot.setLotNo(lotNo);
            lot.setCropGroupName(cropGroupName != null && !cropGroupName.isBlank() ? cropGroupName : "General Crop Group");
            lot.setCropName(cropName != null && !cropName.isBlank() ? cropName : "General Crop");
            lot.setVarietyName(varietyName != null && !varietyName.isBlank() ? varietyName : "General Hybrid");
            lot.setProductName(productName != null && !productName.isBlank() ? productName : "Returned Seed Product");
            lot.setGodownName(godownName);
            lot.setLocation(locationName);
            lot.setMaterialState(materialState);
            lot.setQuantity(qty);
            lot.setOriginalQuantity(qty);
            lot.setNoOfBags(1);
            lot.setOriginalNoOfBags(1);
            lot.setUnit("BAGS");
            lot.setSource("SALES_RETURN_INTAKE");
            try {
                stockLotRepository.save(lot);
                log.info("Created new Inventory Lot {} for Sales Return Intake {}", lotNo, doc.getIntakeSlip());
            } catch (Exception ex) {
                log.warn("Failed to create Inventory Lot for Sales Return Intake: {}", ex.getMessage());
            }
        }
    }
}

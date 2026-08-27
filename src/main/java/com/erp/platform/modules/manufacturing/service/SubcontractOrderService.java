package com.erp.platform.modules.manufacturing.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.SubcontractOrder;
import com.erp.platform.modules.manufacturing.entity.SubcontractOrder.SCOStatus;
import com.erp.platform.modules.manufacturing.repository.SubcontractOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubcontractOrderService {

    private final SubcontractOrderRepository repo;
    private final TenantContext tenantContext;

    public PageResponse<SubcontractOrder> list(SCOStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (status != null) return PageResponse.of(repo.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable));
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public SubcontractOrder getById(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Subcontract order not found: " + id));
    }

    @Transactional
    public SubcontractOrder create(Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        long count = repo.countByTenantId(tenantId) + 1;
        SubcontractOrder sco = new SubcontractOrder();
        sco.setTenantId(tenantId);
        sco.setOrderNumber(String.format("SCO-%04d", count));
        sco.setOrderDate(LocalDate.now());
        sco.setStatus(SCOStatus.DRAFT);
        apply(sco, body);
        return repo.save(sco);
    }

    @Transactional
    public SubcontractOrder update(UUID id, Map<String, Object> body) {
        SubcontractOrder sco = findMutable(id);
        apply(sco, body);
        return repo.save(sco);
    }

    @Transactional
    public SubcontractOrder updateStatus(UUID id, SCOStatus newStatus) {
        SubcontractOrder sco = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Subcontract order not found: " + id));
        sco.setStatus(newStatus);
        if (newStatus == SCOStatus.RECEIVED) sco.setReceivedDate(LocalDate.now());
        return repo.save(sco);
    }

    @Transactional
    public void delete(UUID id) {
        SubcontractOrder sco = findMutable(id);
        sco.setDeletedAt(LocalDateTime.now());
        repo.save(sco);
    }

    private SubcontractOrder findMutable(UUID id) {
        SubcontractOrder sco = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Subcontract order not found: " + id));
        if (sco.getStatus() == SCOStatus.RECEIVED || sco.getStatus() == SCOStatus.CANCELLED)
            throw AppException.badRequest("Cannot modify a completed or cancelled order");
        return sco;
    }

    private void apply(SubcontractOrder sco, Map<String, Object> body) {
        if (body.containsKey("vendorId") && body.get("vendorId") != null)
            sco.setVendorId(UUID.fromString(body.get("vendorId").toString()));
        if (body.containsKey("vendorName")) sco.setVendorName(s(body, "vendorName"));
        if (body.containsKey("productId") && body.get("productId") != null)
            sco.setProductId(UUID.fromString(body.get("productId").toString()));
        if (body.containsKey("productName")) sco.setProductName(s(body, "productName"));
        if (body.containsKey("workOrderId") && body.get("workOrderId") != null)
            sco.setWorkOrderId(UUID.fromString(body.get("workOrderId").toString()));
        if (body.containsKey("quantity") && body.get("quantity") != null)
            sco.setQuantity(new BigDecimal(body.get("quantity").toString()));
        if (body.containsKey("unit")) sco.setUnit(s(body, "unit"));
        if (body.containsKey("unitCost") && body.get("unitCost") != null) {
            BigDecimal uc = new BigDecimal(body.get("unitCost").toString());
            sco.setUnitCost(uc);
            if (sco.getQuantity() != null) sco.setTotalCost(sco.getQuantity().multiply(uc));
        }
        if (body.containsKey("expectedDate") && body.get("expectedDate") != null)
            sco.setExpectedDate(LocalDate.parse(body.get("expectedDate").toString()));
        if (body.containsKey("receivedQty") && body.get("receivedQty") != null)
            sco.setReceivedQty(new BigDecimal(body.get("receivedQty").toString()));
        if (body.containsKey("notes")) sco.setNotes(s(body, "notes"));
    }

    private String s(Map<String, Object> body, String key) {
        Object v = body.get(key); return v != null ? v.toString() : null;
    }
}

package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.dto.CreatePurchaseOrderRequest;
import com.erp.platform.modules.purchase.dto.CreatePurchaseQuotationRequest;
import com.erp.platform.modules.purchase.dto.PurchaseOrderDto;
import com.erp.platform.modules.purchase.dto.PurchaseQuotationDto;
import com.erp.platform.modules.purchase.entity.PurchaseQuotation;
import com.erp.platform.modules.purchase.entity.PurchaseQuotation.PQStatus;
import com.erp.platform.modules.purchase.entity.PurchaseQuotationItem;
import com.erp.platform.modules.purchase.repository.PurchaseQuotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class PurchaseQuotationService {

    private final PurchaseQuotationRepository quotationRepository;
    private final TenantContext tenantContext;
    private final PurchaseOrderService purchaseOrderService;

    public PurchaseQuotationService(PurchaseQuotationRepository quotationRepository,
                                    TenantContext tenantContext,
                                    @Lazy PurchaseOrderService purchaseOrderService) {
        this.quotationRepository = quotationRepository;
        this.tenantContext = tenantContext;
        this.purchaseOrderService = purchaseOrderService;
    }

    public PageResponse<PurchaseQuotationDto> list(UUID requisitionId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = requisitionId != null
                ? quotationRepository.findByTenantIdAndRequisitionIdAndDeletedAtIsNull(tenantId, requisitionId, pageable)
                : quotationRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public PurchaseQuotationDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PurchaseQuotationDto create(CreatePurchaseQuotationRequest request) {
        UUID tenantId = tenantContext.current();
        PurchaseQuotation quotation = new PurchaseQuotation();
        quotation.setTenantId(tenantId);
        quotation.setQuotationNumber(generateQuotationNumber(tenantId));
        quotation.setRequisitionId(request.getRequisitionId());
        quotation.setVendorId(request.getVendorId());
        quotation.setVendorName(request.getVendorName());
        quotation.setQuotationType(request.getQuotationType());
        quotation.setQuotationDate(request.getQuotationDate() != null ? request.getQuotationDate() : LocalDate.now());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setStatus(PQStatus.RECEIVED);
        quotation.setDeliveryDays(request.getDeliveryDays());
        quotation.setPaymentTerms(request.getPaymentTerms());
        quotation.setNotes(request.getNotes());

        List<PurchaseQuotationItem> items = buildItems(quotation, request.getItems(), tenantId);
        quotation.setItems(items);
        calculateTotals(quotation);

        PurchaseQuotation saved = quotationRepository.save(quotation);
        log.info("PurchaseQuotation created: id={}, number={}", saved.getId(), saved.getQuotationNumber());
        return toDto(saved);
    }

    @Transactional
    public PurchaseQuotationDto update(UUID id, CreatePurchaseQuotationRequest request) {
        UUID tenantId = tenantContext.current();
        PurchaseQuotation quotation = findOrThrow(id);
        if (quotation.getStatus() == PQStatus.REJECTED || quotation.getStatus() == PQStatus.SELECTED) {
            throw AppException.badRequest("REJECTED or SELECTED quotations cannot be edited");
        }
        quotation.setVendorId(request.getVendorId());
        quotation.setVendorName(request.getVendorName());
        quotation.setQuotationType(request.getQuotationType());
        if (request.getQuotationDate() != null) quotation.setQuotationDate(request.getQuotationDate());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setDeliveryDays(request.getDeliveryDays());
        quotation.setPaymentTerms(request.getPaymentTerms());
        quotation.setNotes(request.getNotes());
        if (request.getItems() != null) {
            quotation.getItems().clear();
            quotation.getItems().addAll(buildItems(quotation, request.getItems(), tenantId));
            calculateTotals(quotation);
        }
        PurchaseQuotation saved = quotationRepository.save(quotation);
        log.info("PurchaseQuotation updated: id={}", id);
        return toDto(saved);
    }

    @Transactional
    public PurchaseQuotationDto selectQuotation(UUID id) {
        PurchaseQuotation quotation = findOrThrow(id);
        if (quotation.getStatus() == PQStatus.REJECTED) {
            throw AppException.badRequest("Cannot select a rejected quotation");
        }
        quotation.setStatus(PQStatus.SELECTED);
        return toDto(quotationRepository.save(quotation));
    }

    @Transactional
    public PurchaseOrderDto createPoFromQuotation(UUID id) {
        PurchaseQuotation quotation = findOrThrow(id);
        if (quotation.getStatus() == PQStatus.REJECTED) {
            throw AppException.badRequest("Cannot create PO from a rejected quotation");
        }
        if (quotation.getVendorId() == null) {
            throw AppException.badRequest("Quotation has no vendor — cannot create Purchase Order");
        }

        quotation.setStatus(PQStatus.SELECTED);
        quotationRepository.save(quotation);

        CreatePurchaseOrderRequest poRequest = new CreatePurchaseOrderRequest();
        poRequest.setVendorId(quotation.getVendorId());
        poRequest.setOrderDate(LocalDate.now());
        poRequest.setQuotationReference(quotation.getQuotationNumber());
        poRequest.setPaymentTerms(quotation.getPaymentTerms());
        poRequest.setNotes(quotation.getNotes());
        if (quotation.getDeliveryDays() > 0) {
            poRequest.setExpectedDeliveryDate(LocalDate.now().plusDays(quotation.getDeliveryDays()));
        }

        List<CreatePurchaseOrderRequest.POItemRequest> items = quotation.getItems().stream()
                .filter(qi -> qi.getProductId() != null)
                .map(qi -> {
                    CreatePurchaseOrderRequest.POItemRequest item = new CreatePurchaseOrderRequest.POItemRequest();
                    item.setProductId(qi.getProductId());
                    item.setProductName(qi.getProductName());
                    item.setQuantity(qi.getQuantity());
                    item.setUnit(qi.getUnit());
                    item.setUnitPrice(qi.getUnitPrice() != null ? qi.getUnitPrice() : BigDecimal.ZERO);
                    item.setTaxPercent(qi.getTaxPercent() != null ? qi.getTaxPercent() : BigDecimal.ZERO);
                    item.setDiscountPercent(BigDecimal.ZERO);
                    return item;
                })
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            throw AppException.badRequest("No product-linked items in quotation — cannot create Purchase Order");
        }
        poRequest.setItems(items);

        PurchaseOrderDto po = purchaseOrderService.create(poRequest);
        log.info("PO {} created from quotation {}", po.getPoNumber(), quotation.getQuotationNumber());
        return po;
    }

    @Transactional
    public PurchaseQuotationDto updateStatus(UUID id, PQStatus status) {
        PurchaseQuotation quotation = findOrThrow(id);
        quotation.setStatus(status);
        return toDto(quotationRepository.save(quotation));
    }

    @Transactional
    public void delete(UUID id) {
        PurchaseQuotation quotation = findOrThrow(id);
        quotation.setDeletedAt(LocalDateTime.now());
        quotationRepository.save(quotation);
        log.info("PurchaseQuotation soft-deleted: id={}", id);
    }

    private List<PurchaseQuotationItem> buildItems(PurchaseQuotation quotation,
            List<CreatePurchaseQuotationRequest.ItemRequest> requests, UUID tenantId) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            PurchaseQuotationItem item = new PurchaseQuotationItem();
            item.setTenantId(tenantId);
            item.setQuotation(quotation);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setCropGroupId(r.getCropGroupId());
            item.setCropGroupName(r.getCropGroupName());
            item.setCropId(r.getCropId());
            item.setCropName(r.getCropName());
            item.setVarietyId(r.getVarietyId());
            item.setVarietyName(r.getVarietyName());
            item.setQuantity(r.getQuantity());
            item.setUnit(r.getUnit());
            item.setUnitPrice(r.getUnitPrice() != null ? r.getUnitPrice() : BigDecimal.ZERO);
            item.setTaxPercent(r.getTaxPercent() != null ? r.getTaxPercent() : BigDecimal.ZERO);
            item.setDeliveryDays(r.getDeliveryDays());
            item.setRemarks(r.getRemarks());

            BigDecimal lineTotal = item.getUnitPrice().multiply(
                    item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal tax = lineTotal.multiply(item.getTaxPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            item.setTaxAmount(tax);
            item.setTotalAmount(lineTotal.add(tax));
            return item;
        }).collect(Collectors.toList());
    }

    private void calculateTotals(PurchaseQuotation quotation) {
        BigDecimal subtotal = quotation.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(i.getQuantity() != null ? i.getQuantity() : BigDecimal.ONE))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxTotal = quotation.getItems().stream()
                .map(i -> i.getTaxAmount() != null ? i.getTaxAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        quotation.setSubtotal(subtotal);
        quotation.setTaxAmount(taxTotal);
        quotation.setTotalAmount(subtotal.add(taxTotal));
    }

    public PurchaseQuotationDto toDto(PurchaseQuotation q) {
        PurchaseQuotationDto dto = new PurchaseQuotationDto();
        dto.setId(q.getId());
        dto.setTenantId(q.getTenantId());
        dto.setQuotationNumber(q.getQuotationNumber());
        dto.setRequisitionId(q.getRequisitionId());
        dto.setVendorId(q.getVendorId());
        dto.setVendorName(q.getVendorName());
        dto.setQuotationType(q.getQuotationType());
        dto.setQuotationDate(q.getQuotationDate());
        dto.setValidUntil(q.getValidUntil());
        dto.setStatus(q.getStatus());
        dto.setSubtotal(q.getSubtotal());
        dto.setTaxAmount(q.getTaxAmount());
        dto.setTotalAmount(q.getTotalAmount());
        dto.setDeliveryDays(q.getDeliveryDays());
        dto.setPaymentTerms(q.getPaymentTerms());
        dto.setNotes(q.getNotes());
        dto.setCreatedAt(q.getCreatedAt());
        if (q.getItems() != null) {
            dto.setItems(q.getItems().stream().map(i -> {
                PurchaseQuotationDto.ItemDto idto = new PurchaseQuotationDto.ItemDto();
                idto.setId(i.getId());
                idto.setProductId(i.getProductId());
                idto.setProductName(i.getProductName());
                idto.setCropGroupId(i.getCropGroupId());
                idto.setCropGroupName(i.getCropGroupName());
                idto.setCropId(i.getCropId());
                idto.setCropName(i.getCropName());
                idto.setVarietyId(i.getVarietyId());
                idto.setVarietyName(i.getVarietyName());
                idto.setQuantity(i.getQuantity());
                idto.setUnit(i.getUnit());
                idto.setUnitPrice(i.getUnitPrice());
                idto.setTaxPercent(i.getTaxPercent());
                idto.setTaxAmount(i.getTaxAmount());
                idto.setTotalAmount(i.getTotalAmount());
                idto.setDeliveryDays(i.getDeliveryDays());
                idto.setRemarks(i.getRemarks());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private PurchaseQuotation findOrThrow(UUID id) {
        return quotationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Purchase quotation not found: " + id));
    }

    private String generateQuotationNumber(UUID tenantId) {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = quotationRepository.countByTenantIdAndDeletedAtIsNull(tenantId) + 1;
        return String.format("PQ-%s-%03d", year, count);
    }
}

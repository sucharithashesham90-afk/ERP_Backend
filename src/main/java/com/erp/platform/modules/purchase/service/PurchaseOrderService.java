package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.email.EmailService;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.master.repository.VendorRepository;
import com.erp.platform.modules.purchase.dto.CreatePurchaseOrderRequest;
import com.erp.platform.modules.purchase.dto.PurchaseOrderDto;
import com.erp.platform.modules.purchase.entity.GoodsReceipt;
import com.erp.platform.modules.purchase.entity.GoodsReceiptItem;
import com.erp.platform.modules.purchase.entity.PurchaseOrder;
import com.erp.platform.modules.purchase.entity.PurchaseOrder.POStatus;
import com.erp.platform.modules.purchase.entity.PurchaseOrderItem;
import com.erp.platform.modules.purchase.repository.GoodsReceiptRepository;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
import com.erp.platform.modules.workflow.service.WorkflowCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final GoodsReceiptRepository grnRepository;
    private final VendorRepository vendorRepository;
    private final TenantContext tenantContext;
    private final EmailService emailService;

    public PageResponse<PurchaseOrderDto> list(UUID supplierId, POStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        org.springframework.data.domain.Page<com.erp.platform.modules.purchase.entity.PurchaseOrder> page;
        if (supplierId != null && status != null) {
            page = poRepository.findByTenantIdAndVendorIdAndStatusAndDeletedAtIsNull(tenantId, supplierId, status, pageable);
        } else if (supplierId != null) {
            page = poRepository.findByTenantIdAndVendorIdAndDeletedAtIsNull(tenantId, supplierId, pageable);
        } else if (status != null) {
            page = poRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = poRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.of(page.map(this::toDto));
    }

    public PurchaseOrderDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    public PurchaseOrderDto getByNumber(String poNumber) {
        String num = poNumber == null ? "" : poNumber.trim();
        return poRepository.findByTenantIdAndPoNumberIgnoreCaseAndDeletedAtIsNull(tenantContext.current(), num)
                .map(this::toDto)
                .orElseThrow(() -> AppException.notFound("Purchase order not found: " + poNumber));
    }

    @Transactional
    public PurchaseOrderDto create(CreatePurchaseOrderRequest request) {
        UUID tenantId = tenantContext.current();
        Vendor vendor = vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getVendorId())
                .orElseThrow(() -> AppException.notFound("Vendor not found: " + request.getVendorId()));

        PurchaseOrder po = new PurchaseOrder();
        po.setTenantId(tenantId);
        po.setVendorId(vendor.getId());
        po.setVendorName(vendor.getName());
        po.setPoNumber(generatePoNumber());
        po.setStatus(POStatus.DRAFT);
        po.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now());
        po.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        po.setSubject(request.getSubject());
        po.setQuotationReference(request.getQuotationReference());
        po.setDeliveryAddress(request.getDeliveryAddress());
        po.setPaymentTerms(request.getPaymentTerms());
        po.setDeliveryTerms(request.getDeliveryTerms());
        po.setQualityTerms(request.getQualityTerms());
        po.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        po.setFreightCharges(request.getFreightCharges() != null ? request.getFreightCharges() : BigDecimal.ZERO);
        po.setNotes(request.getNotes());
        applyPoHeader(po, request);

        List<PurchaseOrderItem> items = buildItems(po, request.getItems());
        po.setItems(items);
        calculateTotals(po);

        po = poRepository.save(po);
        log.info("PurchaseOrder created: id={}, number={}", po.getId(), po.getPoNumber());
        return toDto(po);
    }

    @Transactional
    public PurchaseOrderDto update(UUID id, CreatePurchaseOrderRequest request) {
        UUID tenantId = tenantContext.current();
        PurchaseOrder po = findOrThrow(id);

        Vendor vendor = vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getVendorId())
                .orElseThrow(() -> AppException.notFound("Vendor not found: " + request.getVendorId()));

        po.setVendorId(vendor.getId());
        po.setVendorName(vendor.getName());
        if (request.getOrderDate() != null) po.setOrderDate(request.getOrderDate());
        po.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        po.setSubject(request.getSubject());
        po.setQuotationReference(request.getQuotationReference());
        po.setDeliveryAddress(request.getDeliveryAddress());
        po.setPaymentTerms(request.getPaymentTerms());
        po.setDeliveryTerms(request.getDeliveryTerms());
        po.setQualityTerms(request.getQualityTerms());
        if (request.getDiscountAmount() != null) po.setDiscountAmount(request.getDiscountAmount());
        if (request.getFreightCharges() != null) po.setFreightCharges(request.getFreightCharges());
        po.setNotes(request.getNotes());
        applyPoHeader(po, request);

        if (request.getItems() != null) {
            po.getItems().clear();
            po.getItems().addAll(buildItems(po, request.getItems()));
            calculateTotals(po);
        }

        po = poRepository.save(po);
        log.info("PurchaseOrder updated: id={}", po.getId());
        return toDto(po);
    }

    @Transactional
    public PurchaseOrderDto updateStatus(UUID id, POStatus status) {
        UUID tenantId = tenantContext.current();
        PurchaseOrder po = findOrThrow(id);
        validateStatusTransition(po.getStatus(), status);
        po.setStatus(status);
        PurchaseOrderDto dto = toDto(poRepository.save(po));

        if (status == POStatus.SENT) {
            vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, po.getVendorId())
                    .ifPresentOrElse(vendor -> {
                        boolean sent = emailService.sendPurchaseOrderToVendor(po, vendor);
                        if (sent) {
                            dto.setEmailSent(true);
                            dto.setEmailMessage("PO emailed to " + vendor.getEmail());
                        } else if (vendor.getEmail() == null || vendor.getEmail().isBlank()) {
                            dto.setEmailSent(false);
                            dto.setEmailMessage("Status updated — vendor has no email on file");
                        } else {
                            dto.setEmailSent(false);
                            dto.setEmailMessage("Status updated — email could not be sent");
                        }
                    }, () -> {
                        dto.setEmailSent(false);
                        dto.setEmailMessage("Status updated — vendor not found");
                    });
        }
        return dto;
    }

    @EventListener
    @Transactional
    public void onWorkflowCompleted(WorkflowCompletedEvent event) {
        if (!"PURCHASE_ORDER".equals(event.getModule())) return;
        poRepository.findByTenantIdAndIdAndDeletedAtIsNull(event.getTenantId(), event.getReferenceId())
                .ifPresent(po -> {
                    if ("APPROVED".equals(event.getOutcome()) && po.getStatus() == POStatus.DRAFT) {
                        po.setStatus(POStatus.CONFIRMED);
                        poRepository.save(po);
                        log.info("PO {} auto-confirmed after workflow approval", po.getPoNumber());
                    } else if ("REJECTED".equals(event.getOutcome()) && po.getStatus() == POStatus.DRAFT) {
                        po.setStatus(POStatus.CANCELLED);
                        poRepository.save(po);
                        log.info("PO {} cancelled after workflow rejection", po.getPoNumber());
                    }
                });
    }

    private void validateStatusTransition(POStatus current, POStatus next) {
        if (current == POStatus.RECEIVED || current == POStatus.PARTIALLY_RECEIVED) {
            if (next == POStatus.CANCELLED) {
                throw AppException.businessRule("Cannot cancel a PO that has already been received");
            }
        }
        if (current == POStatus.CANCELLED) {
            throw AppException.businessRule("Cannot change status of a cancelled PO");
        }
    }

    @Transactional
    public GoodsReceipt receiveGoods(UUID poId, GoodsReceipt grn) {
        UUID tenantId = tenantContext.current();
        PurchaseOrder po = findOrThrow(poId);
        if (po.getStatus() == POStatus.CANCELLED) {
            throw AppException.badRequest("Cannot receive goods for a cancelled PO");
        }
        grn.setTenantId(tenantId);
        grn.setPurchaseOrderId(po.getId());
        grn.setVendorId(po.getVendorId());
        grn.setVendorName(po.getVendorName());
        if (grn.getGrnNumber() == null) {
            grn.setGrnNumber("GRN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                    + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        if (grn.getStatus() == null) grn.setStatus("RECEIVED");
        if (grn.getReceiptDate() == null) grn.setReceiptDate(LocalDate.now());
        if (grn.getItems() != null) grn.getItems().forEach(i -> i.setGoodsReceipt(grn));
        GoodsReceipt saved = grnRepository.save(grn);

        // Update received quantities on PO items and PO status
        boolean allReceived = po.getItems().stream().allMatch(item -> {
            BigDecimal received = item.getReceivedQty() != null ? item.getReceivedQty() : BigDecimal.ZERO;
            return received.compareTo(item.getQuantity()) >= 0;
        });
        po.setStatus(allReceived ? POStatus.RECEIVED : POStatus.PARTIALLY_RECEIVED);
        poRepository.save(po);
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        PurchaseOrder po = findOrThrow(id);
        if (po.getStatus() != POStatus.DRAFT && po.getStatus() != POStatus.CANCELLED) {
            throw AppException.businessRule(
                    "Only DRAFT or CANCELLED purchase orders can be deleted. Current status: " + po.getStatus());
        }
        po.setDeletedAt(LocalDateTime.now());
        poRepository.save(po);
        log.info("PurchaseOrder soft-deleted: id={}", id);
    }

    private List<PurchaseOrderItem> buildItems(PurchaseOrder po,
            List<CreatePurchaseOrderRequest.POItemRequest> requests) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setItemType(r.getItemType());
            item.setCropGroupName(r.getCropGroupName());
            item.setCropName(r.getCropName());
            item.setVarietyName(r.getVarietyName());
            item.setNumberOfBags(r.getNumberOfBags());
            item.setQuantityPerBag(r.getQuantityPerBag());
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setQuantity(r.getQuantity() != null ? r.getQuantity() : BigDecimal.ZERO);
            item.setUnit(r.getUnit());
            item.setUnitPrice(r.getUnitPrice() != null ? r.getUnitPrice() : BigDecimal.ZERO);
            item.setDiscountPercent(r.getDiscountPercent() != null ? r.getDiscountPercent() : BigDecimal.ZERO);
            item.setTaxPercent(r.getTaxPercent() != null ? r.getTaxPercent() : BigDecimal.ZERO);
            return item;
        }).collect(Collectors.toList());
    }

    private void calculateTotals(PurchaseOrder po) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        for (PurchaseOrderItem item : po.getItems()) {
            BigDecimal gross = item.getUnitPrice().multiply(item.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discPct = item.getDiscountPercent() != null ? item.getDiscountPercent() : BigDecimal.ZERO;
            BigDecimal discAmt = gross.multiply(discPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            item.setDiscountAmount(discAmt);
            BigDecimal net = gross.subtract(discAmt);
            BigDecimal tax = net.multiply(item.getTaxPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            item.setTaxAmount(tax);
            item.setTotalAmount(net.add(tax));
            subtotal = subtotal.add(net);
            taxTotal = taxTotal.add(tax);
        }
        po.setSubtotal(subtotal);
        po.setTaxAmount(taxTotal);
        BigDecimal discount = po.getDiscountAmount() != null ? po.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal freight = po.getFreightCharges() != null ? po.getFreightCharges() : BigDecimal.ZERO;
        po.setTotalAmount(subtotal.add(taxTotal).subtract(discount).add(freight));
    }

    /** Copy the seed-industry PO header fields from request onto the entity. */
    private void applyPoHeader(PurchaseOrder po, CreatePurchaseOrderRequest r) {
        po.setPoType(r.getPoType());
        po.setSeedType(r.getSeedType());
        po.setSeedState(r.getSeedState());
        po.setLocation(r.getLocation());
        po.setDeliveryState(r.getDeliveryState());
        po.setDeliveryDistrict(r.getDeliveryDistrict());
        po.setDeliveryCity(r.getDeliveryCity());
        po.setDeliveryZip(r.getDeliveryZip());
        po.setDeliveryPhone(r.getDeliveryPhone());
        po.setSupplierAddress(r.getSupplierAddress());
        po.setSupplierState(r.getSupplierState());
        po.setSupplierDistrict(r.getSupplierDistrict());
        po.setSupplierCity(r.getSupplierCity());
        po.setSupplierZip(r.getSupplierZip());
        po.setSupplierPhone(r.getSupplierPhone());
        po.setSupplierRef(r.getSupplierRef());
        po.setValidityOfOrder(r.getValidityOfOrder());
        po.setInsuranceDetails(r.getInsuranceDetails());
        po.setOtherTerms(r.getOtherTerms());
        po.setSignatureBy(r.getSignatureBy());
        po.setAdvancePercent(r.getAdvancePercent());
    }

    private PurchaseOrderDto toDto(PurchaseOrder po) {
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setId(po.getId());
        dto.setTenantId(po.getTenantId());
        dto.setPoNumber(po.getPoNumber());
        dto.setVendorId(po.getVendorId());
        dto.setVendorName(po.getVendorName());
        dto.setOrderDate(po.getOrderDate());
        dto.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        dto.setStatus(po.getStatus());
        dto.setSubtotal(po.getSubtotal());
        dto.setTaxAmount(po.getTaxAmount());
        dto.setTotalAmount(po.getTotalAmount());
        dto.setSubject(po.getSubject());
        dto.setQuotationReference(po.getQuotationReference());
        dto.setDiscountAmount(po.getDiscountAmount());
        dto.setFreightCharges(po.getFreightCharges());
        dto.setDeliveryAddress(po.getDeliveryAddress());
        dto.setPaymentTerms(po.getPaymentTerms());
        dto.setDeliveryTerms(po.getDeliveryTerms());
        dto.setQualityTerms(po.getQualityTerms());
        dto.setNotes(po.getNotes());
        dto.setPoType(po.getPoType());
        dto.setSeedType(po.getSeedType());
        dto.setSeedState(po.getSeedState());
        dto.setLocation(po.getLocation());
        dto.setDeliveryState(po.getDeliveryState());
        dto.setDeliveryDistrict(po.getDeliveryDistrict());
        dto.setDeliveryCity(po.getDeliveryCity());
        dto.setDeliveryZip(po.getDeliveryZip());
        dto.setDeliveryPhone(po.getDeliveryPhone());
        dto.setSupplierAddress(po.getSupplierAddress());
        dto.setSupplierState(po.getSupplierState());
        dto.setSupplierDistrict(po.getSupplierDistrict());
        dto.setSupplierCity(po.getSupplierCity());
        dto.setSupplierZip(po.getSupplierZip());
        dto.setSupplierPhone(po.getSupplierPhone());
        dto.setSupplierRef(po.getSupplierRef());
        dto.setValidityOfOrder(po.getValidityOfOrder());
        dto.setInsuranceDetails(po.getInsuranceDetails());
        dto.setOtherTerms(po.getOtherTerms());
        dto.setSignatureBy(po.getSignatureBy());
        dto.setAdvancePercent(po.getAdvancePercent());
        dto.setCreatedAt(po.getCreatedAt());

        // Aggregate received quantities across all Goods Receipts for this Purchase Order
        Map<UUID, BigDecimal> receivedByProduct = new HashMap<>();
        try {
            List<com.erp.platform.modules.purchase.entity.GoodsReceipt> grns = grnRepository.findByTenantIdAndPurchaseOrderIdAndDeletedAtIsNull(po.getTenantId(), po.getId());
            for (var grn : grns) {
                if (grn.getItems() != null) {
                    for (var git : grn.getItems()) {
                        if (git.getProductId() != null) {
                            BigDecimal cur = receivedByProduct.getOrDefault(git.getProductId(), BigDecimal.ZERO);
                            BigDecimal addQty = git.getAcceptedQty() != null ? git.getAcceptedQty() : (git.getReceivedQty() != null ? git.getReceivedQty() : BigDecimal.ZERO);
                            receivedByProduct.put(git.getProductId(), cur.add(addQty));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        if (po.getItems() != null) {
            dto.setItems(po.getItems().stream().map(item -> {
                PurchaseOrderDto.POItemDto idto = new PurchaseOrderDto.POItemDto();
                idto.setId(item.getId());
                idto.setItemType(item.getItemType());
                idto.setCropGroupName(item.getCropGroupName());
                idto.setCropName(item.getCropName());
                idto.setVarietyName(item.getVarietyName());
                idto.setNumberOfBags(item.getNumberOfBags());
                idto.setQuantityPerBag(item.getQuantityPerBag());
                idto.setProductId(item.getProductId());
                idto.setProductName(item.getProductName());
                idto.setQuantity(item.getQuantity());
                idto.setUnit(item.getUnit());
                idto.setUnitPrice(item.getUnitPrice());
                idto.setDiscountPercent(item.getDiscountPercent());
                idto.setDiscountAmount(item.getDiscountAmount());
                idto.setTaxPercent(item.getTaxPercent());
                idto.setTaxAmount(item.getTaxAmount());
                idto.setTotalAmount(item.getTotalAmount());
                BigDecimal aggReceived = receivedByProduct.getOrDefault(item.getProductId(), item.getReceivedQty() != null ? item.getReceivedQty() : BigDecimal.ZERO);
                idto.setReceivedQty(aggReceived);
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private PurchaseOrder findOrThrow(UUID id) {
        return poRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Purchase order not found: " + id));
    }

    private String generatePoNumber() {
        return "PO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

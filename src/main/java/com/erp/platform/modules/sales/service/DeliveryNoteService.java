package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.entity.LotStock;
import com.erp.platform.modules.inventory.repository.LotStockRepository;
import com.erp.platform.modules.inventory.service.StockService;
import com.erp.platform.modules.sales.dto.CreateDeliveryNoteRequest;
import com.erp.platform.modules.sales.dto.DeliveryNoteDto;
import com.erp.platform.modules.sales.entity.DeliveryNote;
import com.erp.platform.modules.sales.entity.SalesAllotment;
import com.erp.platform.modules.sales.entity.SalesAllotment.AllotmentStatus;
import com.erp.platform.modules.sales.entity.SalesOrder;
import com.erp.platform.modules.sales.entity.SalesOrderItem;
import com.erp.platform.modules.sales.repository.DeliveryNoteRepository;
import com.erp.platform.modules.sales.repository.SalesAllotmentRepository;
import com.erp.platform.modules.sales.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeliveryNoteService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesAllotmentRepository salesAllotmentRepository;
    private final StockService stockService;
    private final LotStockRepository lotStockRepository;
    private final com.erp.platform.modules.inventory.service.FefoService fefoService;
    private final TenantContext tenantContext;

    public PageResponse<DeliveryNoteDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(deliveryNoteRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    @Transactional
    public DeliveryNoteDto create(CreateDeliveryNoteRequest request) {
        UUID tenantId = tenantContext.current();

        SalesOrder order = null;
        if (request.getSalesOrderId() != null) {
            order = salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getSalesOrderId())
                    .orElseThrow(() -> AppException.notFound("Sales order not found: " + request.getSalesOrderId()));
        }

        DeliveryNote dn = new DeliveryNote();
        dn.setTenantId(tenantId);
        dn.setDeliveryNumber(generateNumber());
        dn.setSalesOrderId(request.getSalesOrderId());
        dn.setCustomerName(order != null ? order.getCustomerName() : null);
        dn.setDeliveryDate(request.getDeliveryDate() != null ? request.getDeliveryDate() : LocalDate.now());
        dn.setDeliveryAddress(request.getDeliveryAddress());
        dn.setCarrierName(request.getCarrierName());
        dn.setTrackingNumber(request.getTrackingNumber());
        dn.setNotes(request.getNotes());
        dn.setStatus("DRAFT");

        dn = deliveryNoteRepository.save(dn);
        log.info("Delivery note created: id={}, number={}", dn.getId(), dn.getDeliveryNumber());
        return toDto(dn);
    }

    @Transactional
    public DeliveryNoteDto deliver(UUID id) {
        UUID tenantId = tenantContext.current();
        DeliveryNote dn = deliveryNoteRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Delivery note not found: " + id));
        if (!"DRAFT".equals(dn.getStatus())) {
            throw AppException.badRequest("Only DRAFT delivery notes can be dispatched");
        }
        // Deduct stock for each item in the linked sales order
        final UUID dnId    = dn.getId();
        final String dnNum = dn.getDeliveryNumber();
        if (dn.getSalesOrderId() != null) {
            salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, dn.getSalesOrderId())
                    .ifPresent(so -> {
                        // Pre-flight: validate stock availability for ALL items before deducting any
                        for (SalesOrderItem item : so.getItems()) {
                            if (item.getProductId() == null || item.getQuantity() == null) continue;
                            boolean hasQuarantineLots = lotStockRepository
                                    .existsByTenantIdAndProductIdAndStatusAndDeletedAtIsNull(
                                            tenantId, item.getProductId(), LotStock.LotStockStatus.QUARANTINE);
                            if (hasQuarantineLots) {
                                boolean hasAvailableLots = lotStockRepository
                                        .existsByTenantIdAndProductIdAndStatusAndDeletedAtIsNull(
                                                tenantId, item.getProductId(), LotStock.LotStockStatus.AVAILABLE);
                                if (!hasAvailableLots) {
                                    throw AppException.badRequest(
                                            "Product '" + item.getProductName() + "' has quarantined lots and no available stock — delivery blocked");
                                }
                            }
                        }
                        // Deduct stock — exceptions propagate to caller (rolls back transaction)
                        for (SalesOrderItem item : so.getItems()) {
                            if (item.getProductId() == null || item.getQuantity() == null) continue;
                            stockService.deductStock(item.getProductId(), null, item.getQuantity(),
                                    "DELIVERY_NOTE", dnId, dnNum,
                                    "PACKED", "GOOD", null, "DISPATCH");
                            // Draw the same quantity from lots, soonest expiry first, so short-dated
                            // stock ships before it becomes a write-off and the dispatch is traceable
                            // to the exact lots that left. Products with no lots are skipped.
                            fefoService.consumeIfLotTracked(item.getProductId(), null, item.getQuantity(),
                                    "DELIVERY_NOTE", dnId, dnNum);
                        }
                        // Update dispatchedQty on any ACTIVE allotment covering this SO's products
                        for (SalesOrderItem item : so.getItems()) {
                            if (item.getProductId() == null || item.getQuantity() == null) continue;
                            java.util.List<SalesAllotment> activeAllotments = salesAllotmentRepository
                                    .findByTenantIdAndProductIdAndStatusAndDeletedAtIsNull(
                                            tenantId, item.getProductId(), AllotmentStatus.ACTIVE);
                            // Match by customer if SO has a customerId
                            activeAllotments.stream()
                                    .filter(a -> so.getCustomerId() == null || so.getCustomerId().equals(a.getCustomerId()))
                                    .findFirst()
                                    .ifPresent(allotment -> {
                                        BigDecimal newDispatched = (allotment.getDispatchedQty() != null
                                                ? allotment.getDispatchedQty() : BigDecimal.ZERO)
                                                .add(item.getQuantity());
                                        allotment.setDispatchedQty(newDispatched);
                                        // Auto-release when fully dispatched
                                        if (newDispatched.compareTo(allotment.getQuantity()) >= 0) {
                                            allotment.setStatus(AllotmentStatus.RELEASED);
                                        }
                                        salesAllotmentRepository.save(allotment);
                                        log.info("Allotment {} dispatched qty updated to {}", allotment.getAllotmentNumber(), newDispatched);
                                    });
                        }
                        so.setStatus(SalesOrder.SalesOrderStatus.SHIPPED);
                        salesOrderRepository.save(so);
                        log.info("Sales order {} marked SHIPPED after delivery note {}", so.getOrderNumber(), dnNum);
                    });
        }
        dn.setStatus("DELIVERED");
        dn = deliveryNoteRepository.save(dn);
        log.info("Delivery note delivered: id={}, number={}", dn.getId(), dn.getDeliveryNumber());
        return toDto(dn);
    }

    @Transactional
    public DeliveryNoteDto createFromSalesOrder(UUID salesOrderId) {
        UUID tenantId = tenantContext.current();
        SalesOrder order = salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, salesOrderId)
                .orElseThrow(() -> AppException.notFound("Sales order not found: " + salesOrderId));
        if (order.getStatus() == SalesOrder.SalesOrderStatus.CANCELLED
                || order.getStatus() == SalesOrder.SalesOrderStatus.DELIVERED) {
            throw AppException.badRequest("Cannot create delivery note for order in status: " + order.getStatus());
        }
        if (deliveryNoteRepository.existsByTenantIdAndSalesOrderIdAndDeletedAtIsNull(tenantId, salesOrderId)) {
            throw AppException.badRequest("A delivery note has already been created for this sales order");
        }
        DeliveryNote dn = new DeliveryNote();
        dn.setTenantId(tenantId);
        dn.setDeliveryNumber(generateNumber());
        dn.setSalesOrderId(salesOrderId);
        dn.setCustomerName(order.getCustomerName());
        dn.setDeliveryDate(order.getDeliveryDate() != null ? order.getDeliveryDate() : LocalDate.now().plusDays(1));
        dn.setDeliveryAddress(order.getShippingAddress());
        dn.setNotes("Auto-created from order " + order.getOrderNumber());
        dn.setStatus("DRAFT");
        dn = deliveryNoteRepository.save(dn);
        log.info("Delivery note {} created from sales order {}", dn.getDeliveryNumber(), order.getOrderNumber());
        return toDto(dn);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        DeliveryNote dn = deliveryNoteRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Delivery note not found: " + id));
        dn.setDeletedAt(LocalDateTime.now());
        deliveryNoteRepository.save(dn);
    }

    private DeliveryNoteDto toDto(DeliveryNote dn) {
        DeliveryNoteDto dto = new DeliveryNoteDto();
        dto.setId(dn.getId());
        dto.setTenantId(dn.getTenantId());
        dto.setDeliveryNumber(dn.getDeliveryNumber());
        dto.setSalesOrderId(dn.getSalesOrderId());
        dto.setCustomerName(dn.getCustomerName());
        dto.setDeliveryDate(dn.getDeliveryDate());
        dto.setDeliveryAddress(dn.getDeliveryAddress());
        dto.setCarrierName(dn.getCarrierName());
        dto.setTrackingNumber(dn.getTrackingNumber());
        dto.setStatus(dn.getStatus());
        dto.setNotes(dn.getNotes());
        dto.setCreatedAt(dn.getCreatedAt());
        return dto;
    }

    private String generateNumber() {
        return "DN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

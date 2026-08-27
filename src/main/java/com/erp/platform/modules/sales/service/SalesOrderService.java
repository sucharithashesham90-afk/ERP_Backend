package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.repository.CustomerRepository;
import com.erp.platform.modules.pricing.entity.PriceList;
import com.erp.platform.modules.pricing.entity.PriceListItem;
import com.erp.platform.modules.pricing.repository.PriceListItemRepository;
import com.erp.platform.modules.pricing.repository.PriceListRepository;
import com.erp.platform.modules.sales.dto.CreateSalesOrderRequest;
import com.erp.platform.modules.sales.dto.SalesOrderDto;
import com.erp.platform.modules.sales.entity.SalesOrder;
import com.erp.platform.modules.sales.entity.SalesOrder.SalesOrderStatus;
import com.erp.platform.modules.sales.entity.SalesOrderItem;
import com.erp.platform.modules.sales.repository.DeliveryNoteRepository;
import com.erp.platform.modules.sales.repository.InvoiceRepository;
import com.erp.platform.modules.sales.repository.SalesOrderRepository;
import com.erp.platform.modules.workflow.entity.WorkflowInstance.WorkflowStatus;
import com.erp.platform.modules.workflow.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final PriceListRepository priceListRepository;
    private final PriceListItemRepository priceListItemRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final InvoiceRepository invoiceRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final TenantContext tenantContext;

    public PageResponse<SalesOrderDto> list(SalesOrderStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? salesOrderRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : salesOrderRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public SalesOrderDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public SalesOrderDto create(CreateSalesOrderRequest request) {
        UUID tenantId = tenantContext.current();
        Customer customer = customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getCustomerId())
                .orElseThrow(() -> AppException.notFound("Customer not found: " + request.getCustomerId()));

        SalesOrder order = new SalesOrder();
        order.setTenantId(tenantId);
        order.setCustomerId(customer.getId());
        order.setCustomerName(customer.getName());
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(SalesOrderStatus.DRAFT);
        order.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingState(request.getShippingState());
        order.setShippingDistrict(request.getShippingDistrict());
        order.setShippingCity(request.getShippingCity());
        order.setShippingCountry(request.getShippingCountry());
        order.setPaymentTerms(request.getPaymentTerms());
        order.setNotes(request.getNotes());
        // A signature arriving with the order is stamped with the time it was accepted; re-saving
        // without one must not silently wipe a signature already given.
        if (request.getSignatureImage() != null && !request.getSignatureImage().isBlank()) {
            order.setSignatureImage(request.getSignatureImage());
            order.setSignedBy(request.getSignedBy());
            order.setSignedAt(java.time.LocalDateTime.now());
        }
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        order.setQuotationId(request.getQuotationId());

        UUID defaultPriceListId = priceListRepository
                .findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(tenantId)
                .map(PriceList::getId).orElse(null);

        List<SalesOrderItem> items = buildItems(order, request.getItems(), defaultPriceListId);
        order.setItems(items);
        calculateTotals(order);

        order = salesOrderRepository.save(order);
        log.info("SalesOrder created: id={}, number={}", order.getId(), order.getOrderNumber());
        return toDto(order);
    }

    @Transactional
    public SalesOrderDto update(UUID id, CreateSalesOrderRequest request) {
        UUID tenantId = tenantContext.current();
        SalesOrder order = findOrThrow(id);

        Customer customer = customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getCustomerId())
                .orElseThrow(() -> AppException.notFound("Customer not found: " + request.getCustomerId()));

        order.setCustomerId(customer.getId());
        order.setCustomerName(customer.getName());
        if (request.getOrderDate() != null) order.setOrderDate(request.getOrderDate());
        order.setDeliveryDate(request.getDeliveryDate());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingState(request.getShippingState());
        order.setShippingDistrict(request.getShippingDistrict());
        order.setShippingCity(request.getShippingCity());
        order.setShippingCountry(request.getShippingCountry());
        order.setPaymentTerms(request.getPaymentTerms());
        order.setNotes(request.getNotes());
        // A signature arriving with the order is stamped with the time it was accepted; re-saving
        // without one must not silently wipe a signature already given.
        if (request.getSignatureImage() != null && !request.getSignatureImage().isBlank()) {
            order.setSignatureImage(request.getSignatureImage());
            order.setSignedBy(request.getSignedBy());
            order.setSignedAt(java.time.LocalDateTime.now());
        }
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);

        if (request.getItems() != null) {
            UUID defaultPriceListId = priceListRepository
                    .findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(tenantId)
                    .map(PriceList::getId).orElse(null);
            order.getItems().clear();
            order.getItems().addAll(buildItems(order, request.getItems(), defaultPriceListId));
            calculateTotals(order);
        }

        order = salesOrderRepository.save(order);
        log.info("SalesOrder updated: id={}", order.getId());
        return toDto(order);
    }

    @Transactional
    public SalesOrderDto updateStatus(UUID id, SalesOrderStatus newStatus) {
        UUID tenantId = tenantContext.current();
        SalesOrder order = findOrThrow(id);
        validateStatusTransition(order.getStatus(), newStatus);
        // Block if a workflow approval is pending for this SO
        if (workflowInstanceRepository.existsByTenantIdAndReferenceIdAndModuleAndStatusAndDeletedAtIsNull(
                tenantId, id, "SALES_ORDER", WorkflowStatus.PENDING)) {
            throw AppException.badRequest("Sales order has a pending workflow approval — status cannot be changed manually");
        }

        if (newStatus == SalesOrderStatus.CONFIRMED && order.getCustomerId() != null) {
            customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, order.getCustomerId())
                    .ifPresent(customer -> {
                        BigDecimal creditLimit = customer.getCreditLimit();
                        if (creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal outstanding = customer.getOutstandingBalance() != null
                                    ? customer.getOutstandingBalance() : BigDecimal.ZERO;
                            BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
                            if (outstanding.add(orderTotal).compareTo(creditLimit) > 0) {
                                throw AppException.badRequest(
                                        String.format("Credit limit exceeded. Limit: %.2f, Outstanding: %.2f, This order: %.2f",
                                                creditLimit, outstanding, orderTotal));
                            }
                        }
                    });
        }

        order.setStatus(newStatus);
        return toDto(salesOrderRepository.save(order));
    }

    @Transactional
    public void delete(UUID id) {
        SalesOrder order = findOrThrow(id);
        order.setDeletedAt(LocalDateTime.now());
        salesOrderRepository.save(order);
        log.info("SalesOrder soft-deleted: id={}", id);
    }

    private void validateStatusTransition(SalesOrderStatus current, SalesOrderStatus next) {
        List<SalesOrderStatus> allowed = switch (current) {
            case DRAFT      -> List.of(SalesOrderStatus.CONFIRMED, SalesOrderStatus.CANCELLED);
            case CONFIRMED  -> List.of(SalesOrderStatus.PROCESSING, SalesOrderStatus.CANCELLED);
            case PROCESSING -> List.of(SalesOrderStatus.SHIPPED, SalesOrderStatus.CANCELLED);
            case SHIPPED    -> List.of(SalesOrderStatus.DELIVERED);
            default         -> List.of();
        };
        if (!allowed.contains(next)) {
            throw AppException.badRequest("Invalid status transition: " + current + " -> " + next);
        }
    }

    private List<SalesOrderItem> buildItems(SalesOrder order,
            List<CreateSalesOrderRequest.OrderItemRequest> requests, UUID defaultPriceListId) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            SalesOrderItem item = new SalesOrderItem();
            item.setSalesOrder(order);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setQuantity(r.getQuantity());
            item.setUnit(r.getUnit());

            // Apply price list price if caller didn't provide a unit price and we have a default price list
            BigDecimal unitPrice = r.getUnitPrice();
            if ((unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) == 0)
                    && defaultPriceListId != null && r.getProductId() != null) {
                List<PriceListItem> priceItems = priceListItemRepository
                        .findByPriceListIdAndProductId(defaultPriceListId, r.getProductId());
                if (!priceItems.isEmpty()) {
                    BigDecimal qty = r.getQuantity() != null ? r.getQuantity() : BigDecimal.ONE;
                    unitPrice = priceItems.stream()
                            .filter(p -> p.getMinQuantity() == null || qty.compareTo(p.getMinQuantity()) >= 0)
                            .filter(p -> p.getMaxQuantity() == null || qty.compareTo(p.getMaxQuantity()) <= 0)
                            .map(PriceListItem::getUnitPrice)
                            .findFirst()
                            .orElse(priceItems.get(0).getUnitPrice());
                    // Apply item-level discount from price list
                    BigDecimal discPct = priceItems.get(0).getDiscountPercent();
                    if (discPct != null && discPct.compareTo(BigDecimal.ZERO) > 0
                            && (r.getDiscountPercent() == null || r.getDiscountPercent().compareTo(BigDecimal.ZERO) == 0)) {
                        r.setDiscountPercent(discPct);
                    }
                }
            }
            item.setUnitPrice(unitPrice != null ? unitPrice : BigDecimal.ZERO);
            item.setDiscountPercent(r.getDiscountPercent() != null ? r.getDiscountPercent() : BigDecimal.ZERO);
            item.setTaxPercent(r.getTaxPercent() != null ? r.getTaxPercent() : BigDecimal.ZERO);
            return item;
        }).collect(Collectors.toList());
    }

    private void calculateTotals(SalesOrder order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        for (SalesOrderItem item : order.getItems()) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(item.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discount = lineTotal.multiply(item.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            lineTotal = lineTotal.subtract(discount);
            BigDecimal tax = lineTotal.multiply(item.getTaxPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            item.setTaxAmount(tax);
            item.setTotalAmount(lineTotal.add(tax));
            subtotal = subtotal.add(lineTotal);
            taxTotal = taxTotal.add(tax);
        }
        BigDecimal disc = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        order.setSubtotal(subtotal);
        order.setTaxAmount(taxTotal);
        order.setTotalAmount(subtotal.add(taxTotal).subtract(disc));
    }

    private SalesOrderDto toDto(SalesOrder so) {
        SalesOrderDto dto = new SalesOrderDto();
        dto.setId(so.getId());
        dto.setTenantId(so.getTenantId());
        dto.setOrderNumber(so.getOrderNumber());
        dto.setCustomerId(so.getCustomerId());
        dto.setCustomerName(so.getCustomerName());
        dto.setQuotationId(so.getQuotationId());
        dto.setOrderDate(so.getOrderDate());
        dto.setDeliveryDate(so.getDeliveryDate());
        dto.setStatus(so.getStatus());
        dto.setSubtotal(so.getSubtotal());
        dto.setTaxAmount(so.getTaxAmount());
        dto.setDiscountAmount(so.getDiscountAmount());
        dto.setTotalAmount(so.getTotalAmount());
        dto.setShippingAddress(so.getShippingAddress());
        dto.setShippingState(so.getShippingState());
        dto.setShippingDistrict(so.getShippingDistrict());
        dto.setShippingCity(so.getShippingCity());
        dto.setShippingCountry(so.getShippingCountry());
        dto.setPaymentTerms(so.getPaymentTerms());
        dto.setNotes(so.getNotes());
        dto.setSignatureImage(so.getSignatureImage());
        dto.setSignedBy(so.getSignedBy());
        dto.setSignedAt(so.getSignedAt());
        dto.setCreatedAt(so.getCreatedAt());
        UUID tenantId = so.getTenantId();
        dto.setHasInvoice(invoiceRepository.existsByTenantIdAndSalesOrderIdAndDeletedAtIsNull(tenantId, so.getId()));
        dto.setHasDeliveryNote(deliveryNoteRepository.existsByTenantIdAndSalesOrderIdAndDeletedAtIsNull(tenantId, so.getId()));
        if (so.getItems() != null) {
            dto.setItems(so.getItems().stream().map(item -> {
                SalesOrderDto.SalesOrderItemDto idto = new SalesOrderDto.SalesOrderItemDto();
                idto.setId(item.getId());
                idto.setProductId(item.getProductId());
                idto.setProductName(item.getProductName());
                idto.setQuantity(item.getQuantity());
                idto.setUnit(item.getUnit());
                idto.setUnitPrice(item.getUnitPrice());
                idto.setDiscountPercent(item.getDiscountPercent());
                idto.setTaxPercent(item.getTaxPercent());
                idto.setTaxAmount(item.getTaxAmount());
                idto.setTotalAmount(item.getTotalAmount());
                idto.setDeliveredQty(item.getDeliveredQty());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private SalesOrder findOrThrow(UUID id) {
        return salesOrderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Sales order not found: " + id));
    }

    private String generateOrderNumber() {
        return "SO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

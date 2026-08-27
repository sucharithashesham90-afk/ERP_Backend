package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.email.EmailService;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.repository.CustomerRepository;
import com.erp.platform.modules.sales.dto.CreateQuotationRequest;
import com.erp.platform.modules.sales.dto.CreateSalesOrderRequest;
import com.erp.platform.modules.sales.dto.QuotationDto;
import com.erp.platform.modules.sales.dto.SalesOrderDto;
import com.erp.platform.modules.sales.entity.Quotation;
import com.erp.platform.modules.sales.entity.Quotation.QuotationStatus;
import com.erp.platform.modules.sales.entity.QuotationItem;
import com.erp.platform.modules.sales.repository.QuotationRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderService salesOrderService;
    private final EmailService emailService;
    private final TenantContext tenantContext;

    public PageResponse<QuotationDto> list(QuotationStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? quotationRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : quotationRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public QuotationDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public QuotationDto create(CreateQuotationRequest request) {
        UUID tenantId = tenantContext.current();
        Customer customer = customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getCustomerId())
                .orElseThrow(() -> AppException.notFound("Customer not found: " + request.getCustomerId()));

        Quotation quotation = new Quotation();
        quotation.setTenantId(tenantId);
        quotation.setCustomerId(customer.getId());
        quotation.setCustomerName(customer.getName());
        quotation.setQuotationNumber(generateQuotationNumber());
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setQuotationDate(request.getQuotationDate() != null ? request.getQuotationDate() : LocalDate.now());
        quotation.setValidUntil(request.getValidUntil() != null ? request.getValidUntil() : LocalDate.now().plusDays(30));
        quotation.setTerms(request.getTerms());
        quotation.setNotes(request.getNotes());
        quotation.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);

        List<QuotationItem> items = buildItems(quotation, request.getItems());
        quotation.setItems(items);
        calculateTotals(quotation);

        quotation = quotationRepository.save(quotation);
        log.info("Quotation created: id={}, number={}", quotation.getId(), quotation.getQuotationNumber());
        return toDto(quotation);
    }

    @Transactional
    public QuotationDto update(UUID id, CreateQuotationRequest request) {
        UUID tenantId = tenantContext.current();
        Quotation quotation = findOrThrow(id);
        if (quotation.getStatus() != QuotationStatus.DRAFT && quotation.getStatus() != QuotationStatus.SENT) {
            throw AppException.badRequest("Only DRAFT or SENT quotations can be edited");
        }

        Customer customer = customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getCustomerId())
                .orElseThrow(() -> AppException.notFound("Customer not found: " + request.getCustomerId()));

        quotation.setCustomerId(customer.getId());
        quotation.setCustomerName(customer.getName());
        if (request.getQuotationDate() != null) quotation.setQuotationDate(request.getQuotationDate());
        if (request.getValidUntil() != null) quotation.setValidUntil(request.getValidUntil());
        quotation.setTerms(request.getTerms());
        quotation.setNotes(request.getNotes());
        quotation.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);

        if (request.getItems() != null) {
            quotation.getItems().clear();
            quotation.getItems().addAll(buildItems(quotation, request.getItems()));
        }
        calculateTotals(quotation);

        quotation = quotationRepository.save(quotation);
        log.info("Quotation updated: id={}", quotation.getId());
        return toDto(quotation);
    }

    @Transactional
    public QuotationDto updateStatus(UUID id, QuotationStatus status) {
        Quotation quotation = findOrThrow(id);
        quotation.setStatus(status);
        return toDto(quotationRepository.save(quotation));
    }

    @Transactional
    public QuotationDto sendToCustomer(UUID id) {
        UUID tenantId = tenantContext.current();
        Quotation quotation = findOrThrow(id);
        if (quotation.getStatus() == QuotationStatus.ACCEPTED || quotation.getStatus() == QuotationStatus.REJECTED) {
            throw AppException.badRequest("Cannot send a quotation in status: " + quotation.getStatus());
        }
        quotation.setStatus(QuotationStatus.SENT);
        QuotationDto dto = toDto(quotationRepository.save(quotation));

        customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, quotation.getCustomerId())
                .ifPresentOrElse(customer -> {
                    boolean sent = emailService.sendQuotationToCustomer(quotation, customer);
                    if (sent) {
                        dto.setEmailSent(true);
                        dto.setEmailMessage("Quotation emailed to " + customer.getEmail());
                    } else if (customer.getEmail() == null || customer.getEmail().isBlank()) {
                        dto.setEmailSent(false);
                        dto.setEmailMessage("Status updated — customer has no email on file");
                    } else {
                        dto.setEmailSent(false);
                        dto.setEmailMessage("Status updated — email could not be sent");
                    }
                }, () -> {
                    dto.setEmailSent(false);
                    dto.setEmailMessage("Status updated — customer not found");
                });
        return dto;
    }

    @Transactional
    public SalesOrderDto convertToOrder(UUID id) {
        Quotation quotation = findOrThrow(id);
        if (quotation.getStatus() == QuotationStatus.REJECTED || quotation.getStatus() == QuotationStatus.EXPIRED) {
            throw AppException.badRequest("Cannot convert a " + quotation.getStatus() + " quotation to an order");
        }
        // Build the sales order request from quotation
        CreateSalesOrderRequest orderRequest = new CreateSalesOrderRequest();
        orderRequest.setCustomerId(quotation.getCustomerId());
        orderRequest.setQuotationId(quotation.getId());
        orderRequest.setDiscountAmount(quotation.getDiscountAmount());
        orderRequest.setNotes(quotation.getNotes());

        if (quotation.getItems() != null) {
            List<CreateSalesOrderRequest.OrderItemRequest> items = quotation.getItems().stream().map(qi -> {
                CreateSalesOrderRequest.OrderItemRequest ir = new CreateSalesOrderRequest.OrderItemRequest();
                ir.setProductId(qi.getProductId());
                ir.setProductName(qi.getProductName());
                ir.setQuantity(qi.getQuantity());
                ir.setUnit(qi.getUnit());
                ir.setUnitPrice(qi.getUnitPrice());
                ir.setDiscountPercent(qi.getDiscountPercent());
                ir.setTaxPercent(qi.getTaxPercent());
                return ir;
            }).collect(Collectors.toList());
            orderRequest.setItems(items);
        }

        // Ensure quotation is marked as accepted
        if (quotation.getStatus() != QuotationStatus.ACCEPTED) {
            quotation.setStatus(QuotationStatus.ACCEPTED);
        }
        quotationRepository.save(quotation);

        return salesOrderService.create(orderRequest);
    }

    @Transactional
    public void delete(UUID id) {
        Quotation quotation = findOrThrow(id);
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT quotations can be deleted");
        }
        quotation.setDeletedAt(LocalDateTime.now());
        quotationRepository.save(quotation);
        log.info("Quotation soft-deleted: id={}", id);
    }

    private List<QuotationItem> buildItems(Quotation quotation,
            List<CreateQuotationRequest.QuotationItemRequest> requests) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            QuotationItem item = new QuotationItem();
            item.setQuotation(quotation);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setCropGroupId(r.getCropGroupId());
            item.setCropGroupName(r.getCropGroupName());
            item.setCropId(r.getCropId());
            item.setCropName(r.getCropName());
            item.setVarietyId(r.getVarietyId());
            item.setVarietyName(r.getVarietyName());
            item.setDescription(r.getDescription());
            item.setQuantity(r.getQuantity());
            item.setUnit(r.getUnit());
            item.setUnitPrice(r.getUnitPrice());
            item.setDiscountPercent(r.getDiscountPercent() != null ? r.getDiscountPercent() : BigDecimal.ZERO);
            item.setTaxPercent(r.getTaxPercent() != null ? r.getTaxPercent() : BigDecimal.ZERO);
            return item;
        }).collect(Collectors.toList());
    }

    private void calculateTotals(Quotation quotation) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        for (QuotationItem item : quotation.getItems()) {
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
        BigDecimal disc = quotation.getDiscountAmount() != null ? quotation.getDiscountAmount() : BigDecimal.ZERO;
        quotation.setSubtotal(subtotal);
        quotation.setTaxAmount(taxTotal);
        quotation.setTotalAmount(subtotal.add(taxTotal).subtract(disc));
    }

    private QuotationDto toDto(Quotation q) {
        QuotationDto dto = new QuotationDto();
        dto.setId(q.getId());
        dto.setTenantId(q.getTenantId());
        dto.setQuotationNumber(q.getQuotationNumber());
        dto.setCustomerId(q.getCustomerId());
        dto.setCustomerName(q.getCustomerName());
        dto.setQuotationDate(q.getQuotationDate());
        dto.setValidUntil(q.getValidUntil());
        dto.setStatus(q.getStatus());
        dto.setSubtotal(q.getSubtotal());
        dto.setTaxAmount(q.getTaxAmount());
        dto.setDiscountAmount(q.getDiscountAmount());
        dto.setTotalAmount(q.getTotalAmount());
        dto.setTerms(q.getTerms());
        dto.setNotes(q.getNotes());
        dto.setCreatedAt(q.getCreatedAt());
        if (q.getItems() != null) {
            dto.setItems(q.getItems().stream().map(item -> {
                QuotationDto.QuotationItemDto idto = new QuotationDto.QuotationItemDto();
                idto.setId(item.getId());
                idto.setProductId(item.getProductId());
                idto.setProductName(item.getProductName());
                idto.setCropGroupId(item.getCropGroupId());
                idto.setCropGroupName(item.getCropGroupName());
                idto.setCropId(item.getCropId());
                idto.setCropName(item.getCropName());
                idto.setVarietyId(item.getVarietyId());
                idto.setVarietyName(item.getVarietyName());
                idto.setDescription(item.getDescription());
                idto.setQuantity(item.getQuantity());
                idto.setUnit(item.getUnit());
                idto.setUnitPrice(item.getUnitPrice());
                idto.setDiscountPercent(item.getDiscountPercent());
                idto.setTaxPercent(item.getTaxPercent());
                idto.setTaxAmount(item.getTaxAmount());
                idto.setTotalAmount(item.getTotalAmount());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private Quotation findOrThrow(UUID id) {
        return quotationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Quotation not found: " + id));
    }

    private String generateQuotationNumber() {
        return "QT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

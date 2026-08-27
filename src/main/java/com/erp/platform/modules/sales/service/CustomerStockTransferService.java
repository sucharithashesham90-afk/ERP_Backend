package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.dto.CreateCustomerStockTransferRequest;
import com.erp.platform.modules.sales.dto.CustomerStockTransferDto;
import com.erp.platform.modules.sales.entity.CustomerStockTransfer;
import com.erp.platform.modules.sales.entity.CustomerTransferLine;
import com.erp.platform.modules.sales.repository.CustomerStockTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerStockTransferService {

    private final CustomerStockTransferRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<CustomerStockTransferDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public CustomerStockTransferDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public CustomerStockTransferDto create(CreateCustomerStockTransferRequest request) {
        UUID tenantId = tenantContext.current();
        CustomerStockTransfer e = new CustomerStockTransfer();
        e.setTenantId(tenantId);
        apply(e, request);
        if (e.getTransferNumber() == null || e.getTransferNumber().isBlank())
            e.setTransferNumber(generateCode(tenantId));
        if (e.getStatus() == null || e.getStatus().isBlank()) e.setStatus("DRAFT");
        e = repository.save(e);
        log.info("CustomerStockTransfer created: {}", e.getTransferNumber());
        return toDto(e);
    }

    @Transactional
    public CustomerStockTransferDto update(UUID id, CreateCustomerStockTransferRequest request) {
        CustomerStockTransfer e = findOrThrow(id);
        apply(e, request);
        return toDto(repository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        CustomerStockTransfer e = findOrThrow(id);
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private void apply(CustomerStockTransfer e, CreateCustomerStockTransferRequest r) {
        if (r.transferNumber() != null && !r.transferNumber().isBlank()) e.setTransferNumber(r.transferNumber());
        e.setTransferDate(r.transferDate());
        e.setFromCustomer(r.fromCustomer());
        e.setToCustomer(r.toCustomer());
        e.setFromAddress1(r.fromAddress1());
        e.setFromAddress2(r.fromAddress2());
        e.setFromState(r.fromState());
        e.setFromDistrict(r.fromDistrict());
        e.setFromCity(r.fromCity());
        e.setFromZip(r.fromZip());
        e.setFromPhone(r.fromPhone());
        e.setAddress1(r.address1());
        e.setAddress2(r.address2());
        e.setState(r.state());
        e.setDistrict(r.district());
        e.setCity(r.city());
        e.setZip(r.zip());
        e.setPhone(r.phone());
        e.setFreightTotal(r.freightTotal());
        e.setFreightPaidAmount(r.freightPaidAmount());
        e.setFreightToPay(r.freightToPay() != null ? r.freightToPay()
                : nz(r.freightTotal()).subtract(nz(r.freightPaidAmount())));
        e.setDescription(r.description());
        e.setLocation(r.location());
        e.setDispatchLocation(r.dispatchLocation());
        e.setDealerStockCode(r.dealerStockCode());
        if (r.status() != null) e.setStatus(r.status());

        if (e.getItems() == null) e.setItems(new ArrayList<>());
        e.getItems().clear();
        BigDecimal fromTotal = BigDecimal.ZERO, toTotal = BigDecimal.ZERO;
        if (r.items() != null) {
            for (CreateCustomerStockTransferRequest.Item it : r.items()) {
                CustomerTransferLine l = new CustomerTransferLine();
                l.setCropGroup(it.cropGroup());
                l.setCrop(it.crop());
                l.setVariety(it.variety());
                l.setCropVariety(it.cropVariety());
                l.setProductId(it.productId());
                l.setProductName(it.productName());
                l.setLotNumber(it.lotNumber());
                l.setPacks(it.packs());
                l.setPacksDamaged(it.packsDamaged());
                l.setFromUnitPrice(it.fromUnitPrice());
                l.setFromDiscount(it.fromDiscount());
                l.setFromStCst(it.fromStCst());
                l.setToUnitPrice(it.toUnitPrice());
                l.setToDiscount(it.toDiscount());
                l.setToStCst(it.toStCst());
                // Recompute prices server-side so totals are trustworthy.
                BigDecimal fromPrice = nz(it.packs()).multiply(nz(it.fromUnitPrice()))
                        .subtract(nz(it.fromDiscount())).subtract(nz(it.fromStCst()));
                BigDecimal toPrice = nz(it.packs()).multiply(nz(it.toUnitPrice()))
                        .subtract(nz(it.toDiscount())).subtract(nz(it.toStCst()));
                l.setFromCustPrice(fromPrice);
                l.setToCustPrice(toPrice);
                l.setFromCustomerAmount(fromPrice);
                l.setToCustomerAmount(toPrice);
                fromTotal = fromTotal.add(fromPrice);
                toTotal = toTotal.add(toPrice);
                e.getItems().add(l);
            }
        }
        e.setFromAmount(fromTotal);
        e.setToAmount(toTotal);
        // keep the legacy single-line header in sync with the first line (list display / fallback)
        if (!e.getItems().isEmpty()) {
            CustomerTransferLine first = e.getItems().get(0);
            e.setLotNumber(first.getLotNumber());
            e.setProductName(first.getProductName());
        }
    }

    private String generateCode(UUID tenantId) {
        long n = repository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 1)).getTotalElements();
        return String.format("DST-%d-%05d", LocalDate.now().getYear(), n + 1);
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private CustomerStockTransfer findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("CustomerStockTransfer not found: " + id));
    }

    private CustomerStockTransferDto toDto(CustomerStockTransfer e) {
        List<CustomerStockTransferDto.Item> items = new ArrayList<>();
        if (e.getItems() != null) {
            for (CustomerTransferLine l : e.getItems()) {
                items.add(new CustomerStockTransferDto.Item(
                        l.getCropGroup(), l.getCrop(), l.getVariety(), l.getCropVariety(),
                        l.getProductId(), l.getProductName(), l.getLotNumber(),
                        l.getPacks(), l.getPacksDamaged(),
                        l.getFromUnitPrice(), l.getFromDiscount(), l.getFromStCst(), l.getFromCustPrice(),
                        l.getToUnitPrice(), l.getToDiscount(), l.getToStCst(), l.getToCustPrice(),
                        l.getFromCustomerAmount(), l.getToCustomerAmount()));
            }
        }
        return new CustomerStockTransferDto(
                e.getId(), e.getTransferNumber(), e.getTransferDate(),
                e.getFromCustomer(), e.getToCustomer(),
                e.getFromAddress1(), e.getFromAddress2(), e.getFromState(), e.getFromDistrict(), e.getFromCity(), e.getFromZip(), e.getFromPhone(),
                e.getAddress1(), e.getAddress2(), e.getState(), e.getDistrict(), e.getCity(), e.getZip(), e.getPhone(),
                e.getFreightTotal(), e.getFreightPaidAmount(), e.getFreightToPay(),
                e.getDescription(), e.getLocation(), e.getDispatchLocation(), e.getDealerStockCode(),
                e.getStatus(),
                e.getFromAmount(), e.getToAmount(), e.isPosted(),
                e.getToInvoiceNumber(), e.getFromCreditNoteNumber(), items);
    }
}

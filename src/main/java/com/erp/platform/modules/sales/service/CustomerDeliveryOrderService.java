package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.dto.CreateCustomerDeliveryOrderRequest;
import com.erp.platform.modules.sales.dto.CustomerDeliveryOrderDto;
import com.erp.platform.modules.sales.entity.CustomerDeliveryOrder;
import com.erp.platform.modules.sales.repository.CustomerDeliveryOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerDeliveryOrderService {

    private final CustomerDeliveryOrderRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<CustomerDeliveryOrderDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public CustomerDeliveryOrderDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public CustomerDeliveryOrderDto create(CreateCustomerDeliveryOrderRequest request) {
        UUID tenantId = tenantContext.current();
        CustomerDeliveryOrder entity = new CustomerDeliveryOrder();
        entity.setTenantId(tenantId);
        entity.setDoNumber(request.doNumber());
        entity.setDoDate(request.doDate());
        entity.setSalesOrderNumber(request.salesOrderNumber());
        entity.setCustomerName(request.customerName());
        entity.setLorryNumber(request.lorryNumber());
        entity.setCarrier(request.carrier());
        entity.setFreightToPay(request.freightToPay());
        entity.setDeliveredFrom(request.deliveredFrom());
        entity.setStatus(request.status() != null ? request.status() : "DRAFT");
        entity = repository.save(entity);
        log.info("CustomerDeliveryOrder created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public CustomerDeliveryOrderDto update(UUID id, CreateCustomerDeliveryOrderRequest request) {
        CustomerDeliveryOrder entity = findOrThrow(id);
        entity.setDoNumber(request.doNumber());
        entity.setDoDate(request.doDate());
        entity.setSalesOrderNumber(request.salesOrderNumber());
        entity.setCustomerName(request.customerName());
        entity.setLorryNumber(request.lorryNumber());
        entity.setCarrier(request.carrier());
        entity.setFreightToPay(request.freightToPay());
        entity.setDeliveredFrom(request.deliveredFrom());
        if (request.status() != null) entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        CustomerDeliveryOrder entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private CustomerDeliveryOrder findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("CustomerDeliveryOrder not found: " + id));
    }

    private CustomerDeliveryOrderDto toDto(CustomerDeliveryOrder e) {
        return new CustomerDeliveryOrderDto(
                e.getId(),
                e.getDoNumber(),
                e.getDoDate(),
                e.getSalesOrderNumber(),
                e.getCustomerName(),
                e.getLorryNumber(),
                e.getCarrier(),
                e.getFreightToPay(),
                e.getDeliveredFrom(),
                e.getStatus()
        );
    }
}

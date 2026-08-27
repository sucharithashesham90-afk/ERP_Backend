package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.entity.ServiceCategory;
import com.erp.platform.modules.purchase.entity.ServiceDefinition;
import com.erp.platform.modules.purchase.entity.ServiceOrder;
import com.erp.platform.modules.purchase.entity.ServiceOrder.ServiceOrderStatus;
import com.erp.platform.modules.purchase.entity.ServiceOrderItem;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.master.repository.VendorRepository;
import com.erp.platform.modules.purchase.repository.ServiceCategoryRepository;
import com.erp.platform.modules.purchase.repository.ServiceDefinitionRepository;
import com.erp.platform.modules.purchase.repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicePurchaseService {

    private final ServiceCategoryRepository categoryRepo;
    private final ServiceDefinitionRepository serviceRepo;
    private final ServiceOrderRepository orderRepo;
    private final VendorRepository vendorRepository;
    private final TenantContext tenantContext;

    public PageResponse<ServiceCategory> listCategories(Pageable pageable) {
        return PageResponse.of(categoryRepo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    @Transactional
    public ServiceCategory createCategory(ServiceCategory req) {
        req.setTenantId(tenantContext.current());
        return categoryRepo.save(req);
    }

    @Transactional
    public ServiceCategory updateCategory(UUID id, ServiceCategory req) {
        ServiceCategory e = categoryRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Service category not found: " + id));
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setDescription(req.getDescription());
        e.setActive(req.isActive());
        return categoryRepo.save(e);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        ServiceCategory e = categoryRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Service category not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        categoryRepo.save(e);
    }

    public PageResponse<ServiceDefinition> listServices(UUID categoryId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (categoryId != null) {
            return PageResponse.of(serviceRepo.findByTenantIdAndCategoryIdAndDeletedAtIsNull(tenantId, categoryId, pageable));
        }
        return PageResponse.of(serviceRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    @Transactional
    public ServiceDefinition createService(ServiceDefinition req) {
        req.setTenantId(tenantContext.current());
        return serviceRepo.save(req);
    }

    @Transactional
    public ServiceDefinition updateService(UUID id, ServiceDefinition req) {
        ServiceDefinition e = serviceRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Service not found: " + id));
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setCategory(req.getCategory());
        e.setDescription(req.getDescription());
        e.setUnit(req.getUnit());
        e.setStandardRate(req.getStandardRate());
        e.setTaxRate(req.getTaxRate());
        e.setActive(req.isActive());
        return serviceRepo.save(e);
    }

    @Transactional
    public void deleteService(UUID id) {
        ServiceDefinition e = serviceRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Service not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        serviceRepo.save(e);
    }

    public PageResponse<ServiceOrder> listOrders(Pageable pageable) {
        return PageResponse.of(orderRepo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    public ServiceOrder getOrder(UUID id) {
        return orderRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Service order not found: " + id));
    }

    @Transactional
    public ServiceOrder createOrder(ServiceOrder req) {
        UUID tenantId = tenantContext.current();
        if (req.getVendor() == null || req.getVendor().getId() == null) {
            throw AppException.badRequest("Vendor is required");
        }
        Vendor vendor = vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getVendor().getId())
                .orElseThrow(() -> AppException.badRequest("Vendor not found: " + req.getVendor().getId()));
        req.setVendor(vendor);
        req.setTenantId(tenantId);
        req.setOrderNumber(generateOrderNumber());
        req.setStatus(ServiceOrderStatus.DRAFT);

        if (req.getItems() != null) {
            BigDecimal total = BigDecimal.ZERO;
            for (ServiceOrderItem item : req.getItems()) {
                item.setServiceOrder(req);
                item.setTenantId(tenantId);
                BigDecimal lineTotal = item.getUnitRate().multiply(item.getQuantity()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal tax = lineTotal.multiply(item.getTaxPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                item.setTotalAmount(lineTotal.add(tax));
                total = total.add(item.getTotalAmount());
            }
            req.setTotalAmount(total);
        }

        return orderRepo.save(req);
    }

    @Transactional
    public ServiceOrder updateOrder(UUID id, ServiceOrder req) {
        UUID tenantId = tenantContext.current();
        ServiceOrder existing = orderRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Service order not found: " + id));
        if (existing.getStatus() != ServiceOrderStatus.DRAFT && existing.getStatus() != ServiceOrderStatus.APPROVED) {
            throw AppException.badRequest("Only DRAFT or APPROVED orders can be edited");
        }
        existing.setVendor(req.getVendor());
        existing.setOrderDate(req.getOrderDate());
        existing.setExpectedCompletionDate(req.getExpectedCompletionDate());
        existing.setDescription(req.getDescription());
        existing.setNotes(req.getNotes());
        if (req.getItems() != null) {
            existing.getItems().clear();
            BigDecimal total = BigDecimal.ZERO;
            for (ServiceOrderItem item : req.getItems()) {
                item.setServiceOrder(existing);
                item.setTenantId(tenantId);
                if (item.getTotalAmount() == null || item.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal lineTotal = item.getUnitRate().multiply(item.getQuantity()).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal tax = lineTotal.multiply(item.getTaxPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    item.setTotalAmount(lineTotal.add(tax));
                }
                total = total.add(item.getTotalAmount());
                existing.getItems().add(item);
            }
            existing.setTotalAmount(total);
        }
        return orderRepo.save(existing);
    }

    @Transactional
    public ServiceOrder updateOrderStatus(UUID id, String status) {
        ServiceOrder order = orderRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Service order not found: " + id));
        order.setStatus(ServiceOrderStatus.valueOf(status));
        return orderRepo.save(order);
    }

    @Transactional
    public void deleteOrder(UUID id) {
        ServiceOrder e = orderRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Service order not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        orderRepo.save(e);
    }

    private String generateOrderNumber() {
        return "SO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}

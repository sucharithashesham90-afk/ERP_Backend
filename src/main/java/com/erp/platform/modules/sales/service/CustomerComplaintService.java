package com.erp.platform.modules.sales.service;
import java.util.UUID;

import com.erp.platform.modules.sales.dto.CreateCustomerComplaintRequest;
import com.erp.platform.modules.sales.dto.CustomerComplaintDto;
import com.erp.platform.modules.sales.entity.CustomerComplaint;
import com.erp.platform.modules.sales.repository.CustomerComplaintRepository;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerComplaintService {

    private final CustomerComplaintRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<CustomerComplaintDto> list(int page, int size) {
        UUID tid = tenantContext.current();
        Page<CustomerComplaint> p = repository.findByTenantIdAndDeletedAtIsNull(
                tid, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.map(this::toDto));
    }

    public CustomerComplaintDto create(CreateCustomerComplaintRequest req) {
        CustomerComplaint e = new CustomerComplaint();
        e.setTenantId(tenantContext.current());
        e.setComplaintNumber(req.complaintNumber());
        e.setComplaintDate(req.complaintDate());
        e.setCustomerName(req.customerName());
        e.setPhoneNumber(req.phoneNumber());
        e.setProductName(req.productName());
        e.setLotNumber(req.lotNumber());
        e.setComplaintType(req.complaintType());
        e.setDescription(req.description());
        e.setAssignedTo(req.assignedTo());
        e.setResolutionDate(req.resolutionDate());
        e.setResolution(req.resolution());
        e.setStatus(req.status() != null ? req.status() : "OPEN");
        return toDto(repository.save(e));
    }

    public CustomerComplaintDto update(UUID id, CreateCustomerComplaintRequest req) {
        CustomerComplaint e = repository.findById(id).orElseThrow();
        e.setComplaintNumber(req.complaintNumber());
        e.setComplaintDate(req.complaintDate());
        e.setCustomerName(req.customerName());
        e.setPhoneNumber(req.phoneNumber());
        e.setProductName(req.productName());
        e.setLotNumber(req.lotNumber());
        e.setComplaintType(req.complaintType());
        e.setDescription(req.description());
        e.setAssignedTo(req.assignedTo());
        e.setResolutionDate(req.resolutionDate());
        e.setResolution(req.resolution());
        e.setStatus(req.status());
        return toDto(repository.save(e));
    }

    public void delete(UUID id) {
        CustomerComplaint e = repository.findById(id).orElseThrow();
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private CustomerComplaintDto toDto(CustomerComplaint e) {
        return new CustomerComplaintDto(
                e.getId(), e.getComplaintNumber(), e.getComplaintDate(),
                e.getCustomerName(), e.getPhoneNumber(), e.getProductName(),
                e.getLotNumber(), e.getComplaintType(), e.getDescription(),
                e.getAssignedTo(), e.getResolutionDate(), e.getResolution(),
                e.getStatus());
    }
}


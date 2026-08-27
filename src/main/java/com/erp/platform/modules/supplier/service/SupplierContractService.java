package com.erp.platform.modules.supplier.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.supplier.dto.CreateSupplierContractRequest;
import com.erp.platform.modules.supplier.dto.SupplierContractDto;
import com.erp.platform.modules.supplier.entity.ContractItem;
import com.erp.platform.modules.supplier.entity.SupplierContract;
import com.erp.platform.modules.supplier.entity.SupplierContract.ContractStatus;
import com.erp.platform.modules.supplier.repository.SupplierContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupplierContractService {

    private final SupplierContractRepository contractRepo;
    private final TenantContext tenantContext;

    public PageResponse<SupplierContractDto> list(UUID vendorId, ContractStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = vendorId != null && status != null
                ? contractRepo.findByTenantIdAndVendorIdAndStatusAndDeletedAtIsNull(tenantId, vendorId, status, pageable)
                : vendorId != null
                ? contractRepo.findByTenantIdAndVendorIdAndDeletedAtIsNull(tenantId, vendorId, pageable)
                : status != null
                ? contractRepo.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : contractRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public SupplierContractDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public SupplierContractDto create(CreateSupplierContractRequest request) {
        UUID tenantId = tenantContext.current();
        SupplierContract contract = new SupplierContract();
        contract.setTenantId(tenantId);
        contract.setContractNumber(generateNumber(tenantId));
        mapRequest(contract, request);
        if (contract.getStatus() == null) contract.setStatus(ContractStatus.DRAFT);
        contract.setItems(buildItems(contract, request.getItems()));
        contract = contractRepo.save(contract);
        log.info("SupplierContract created: {}", contract.getContractNumber());
        return toDto(contract);
    }

    @Transactional
    public SupplierContractDto update(UUID id, CreateSupplierContractRequest request) {
        SupplierContract contract = findOrThrow(id);
        mapRequest(contract, request);
        if (request.getItems() != null) {
            contract.getItems().clear();
            contract.getItems().addAll(buildItems(contract, request.getItems()));
        }
        return toDto(contractRepo.save(contract));
    }

    @Transactional
    public SupplierContractDto terminate(UUID id, String reason) {
        SupplierContract contract = findOrThrow(id);
        if (contract.getStatus() == ContractStatus.TERMINATED) {
            throw AppException.businessRule("Contract is already terminated");
        }
        contract.setStatus(ContractStatus.TERMINATED);
        contract.setNotes((contract.getNotes() != null ? contract.getNotes() + "\n" : "") + "Terminated: " + reason);
        return toDto(contractRepo.save(contract));
    }

    @Transactional
    public void delete(UUID id) {
        SupplierContract contract = findOrThrow(id);
        contract.setDeletedAt(LocalDateTime.now());
        contractRepo.save(contract);
    }

    private void mapRequest(SupplierContract c, CreateSupplierContractRequest r) {
        if (r.getVendorId() != null) c.setVendorId(r.getVendorId());
        if (r.getVendorName() != null) c.setVendorName(r.getVendorName());
        if (r.getContractType() != null) c.setContractType(r.getContractType());
        if (r.getStatus() != null) c.setStatus(r.getStatus());
        c.setStartDate(r.getStartDate());
        c.setEndDate(r.getEndDate());
        c.setTotalContractValue(r.getTotalContractValue());
        if (r.getCurrency() != null) c.setCurrency(r.getCurrency());
        c.setPaymentTerms(r.getPaymentTerms());
        c.setDeliveryTerms(r.getDeliveryTerms());
        c.setQualityTerms(r.getQualityTerms());
        c.setPenaltyClause(r.getPenaltyClause());
        if (r.getNoticePeriodDays() != null) c.setNoticePeriodDays(r.getNoticePeriodDays());
        if (r.getAutoRenew() != null) c.setAutoRenew(r.getAutoRenew());
        if (r.getRenewalTermMonths() != null) c.setRenewalTermMonths(r.getRenewalTermMonths());
        c.setNotes(r.getNotes());
    }

    private List<ContractItem> buildItems(SupplierContract contract, List<CreateSupplierContractRequest.ContractItemRequest> requests) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            ContractItem item = new ContractItem();
            item.setTenantId(contract.getTenantId());
            item.setContract(contract);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setAgreedPrice(r.getAgreedPrice());
            item.setMinQuantity(r.getMinQuantity());
            item.setMaxQuantity(r.getMaxQuantity());
            item.setUnit(r.getUnit());
            if (r.getLeadTimeDays() != null) item.setLeadTimeDays(r.getLeadTimeDays());
            item.setQualitySpec(r.getQualitySpec());
            return item;
        }).collect(Collectors.toList());
    }

    private SupplierContractDto toDto(SupplierContract c) {
        SupplierContractDto dto = new SupplierContractDto();
        dto.setId(c.getId());
        dto.setTenantId(c.getTenantId());
        dto.setContractNumber(c.getContractNumber());
        dto.setVendorId(c.getVendorId());
        dto.setVendorName(c.getVendorName());
        dto.setContractType(c.getContractType());
        dto.setStatus(c.getStatus());
        dto.setStartDate(c.getStartDate());
        dto.setEndDate(c.getEndDate());
        dto.setTotalContractValue(c.getTotalContractValue());
        dto.setCurrency(c.getCurrency());
        dto.setPaymentTerms(c.getPaymentTerms());
        dto.setDeliveryTerms(c.getDeliveryTerms());
        dto.setQualityTerms(c.getQualityTerms());
        dto.setPenaltyClause(c.getPenaltyClause());
        dto.setNoticePeriodDays(c.getNoticePeriodDays());
        dto.setAutoRenew(c.isAutoRenew());
        dto.setRenewalTermMonths(c.getRenewalTermMonths());
        dto.setNotes(c.getNotes());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        if (c.getItems() != null) {
            dto.setItems(c.getItems().stream().map(item -> {
                SupplierContractDto.ItemDto idto = new SupplierContractDto.ItemDto();
                idto.setId(item.getId());
                idto.setProductId(item.getProductId());
                idto.setProductName(item.getProductName());
                idto.setAgreedPrice(item.getAgreedPrice());
                idto.setMinQuantity(item.getMinQuantity());
                idto.setMaxQuantity(item.getMaxQuantity());
                idto.setUnit(item.getUnit());
                idto.setLeadTimeDays(item.getLeadTimeDays());
                idto.setQualitySpec(item.getQualitySpec());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private SupplierContract findOrThrow(UUID id) {
        return contractRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Supplier contract not found: " + id));
    }

    private String generateNumber(UUID tenantId) {
        long count = contractRepo.countByTenantId(tenantId) + 1;
        return String.format("SC-%d-%03d", Year.now().getValue(), count);
    }
}

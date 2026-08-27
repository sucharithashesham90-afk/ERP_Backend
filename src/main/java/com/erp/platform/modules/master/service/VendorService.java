package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.service.PartyLedgerService;
import com.erp.platform.modules.master.dto.CreateVendorRequest;
import com.erp.platform.modules.master.dto.UpdateVendorRequest;
import com.erp.platform.modules.master.dto.VendorDto;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.master.mapper.VendorMapper;
import com.erp.platform.modules.master.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;
    private final PartyLedgerService partyLedgerService;
    private final TenantContext tenantContext;

    public PageResponse<VendorDto> list(String search, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = StringUtils.hasText(search)
                ? vendorRepository.searchByTenantId(tenantId, search, pageable)
                : vendorRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(vendorMapper::toDto));
    }

    public VendorDto getById(UUID id) {
        return vendorMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public VendorDto create(CreateVendorRequest request) {
        UUID tenantId = tenantContext.current();
        if (StringUtils.hasText(request.getEmail())
                && vendorRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(tenantId, request.getEmail())) {
            throw AppException.conflict("Vendor with email already exists: " + request.getEmail());
        }
        Vendor vendor = vendorMapper.toEntity(request);
        vendor.setTenantId(tenantId);
        if (!StringUtils.hasText(vendor.getCode())) {
            vendor.setCode("VEND-" + System.currentTimeMillis());
        }
        vendor = vendorRepository.save(vendor);
        // Give the supplier its own ledger under Sundry Creditors so purchase invoices
        // post against it and show up in a supplier ledger search.
        partyLedgerService.ensureLedger(PartyLedgerService.PartyType.VENDOR,
                vendor.getName(), vendor.getId(), vendor.getCode());
        log.info("Vendor created: id={}, name={}", vendor.getId(), vendor.getName());
        return vendorMapper.toDto(vendor);
    }

    @Transactional
    public VendorDto update(UUID id, UpdateVendorRequest request) {
        Vendor vendor = findOrThrow(id);
        vendorMapper.updateEntity(request, vendor);
        Vendor saved = vendorRepository.save(vendor);
        partyLedgerService.ensureLedger(PartyLedgerService.PartyType.VENDOR,
                saved.getName(), saved.getId(), saved.getCode());
        return vendorMapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Vendor vendor = findOrThrow(id);
        vendor.setDeletedAt(LocalDateTime.now());
        vendorRepository.save(vendor);
        log.info("Vendor soft-deleted: id={}", id);
    }

    private Vendor findOrThrow(UUID id) {
        return vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Vendor not found: " + id));
    }
}

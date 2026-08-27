package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateTaxRequest;
import com.erp.platform.modules.master.dto.TaxDto;
import com.erp.platform.modules.master.entity.Tax;
import com.erp.platform.modules.master.mapper.TaxMapper;
import com.erp.platform.modules.master.repository.TaxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TaxService {

    private final TaxRepository taxRepository;
    private final TaxMapper taxMapper;
    private final TenantContext tenantContext;

    public PageResponse<TaxDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<Tax> taxes = taxRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        List<TaxDto> dtos = taxes.stream().map(taxMapper::toDto).toList();
        // Wrap in page response from the full list (tax list is typically small)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<TaxDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public TaxDto getById(UUID id) {
        return taxMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public TaxDto create(CreateTaxRequest request) {
        UUID tenantId = tenantContext.current();
        Tax tax = taxMapper.toEntity(request);
        tax.setTenantId(tenantId);
        tax.setActive(true);
        tax = taxRepository.save(tax);
        log.info("Tax created: id={}, name={}", tax.getId(), tax.getName());
        return taxMapper.toDto(tax);
    }

    @Transactional
    public TaxDto update(UUID id, CreateTaxRequest request) {
        Tax tax = findOrThrow(id);
        tax.setName(request.getName());
        tax.setTaxType(request.getTaxType());
        tax.setType(request.getType());
        tax.setRate(request.getRate());
        tax.setCompound(request.isCompound());
        tax.setDescription(request.getDescription());
        return taxMapper.toDto(taxRepository.save(tax));
    }

    @Transactional
    public void delete(UUID id) {
        Tax tax = findOrThrow(id);
        tax.setDeletedAt(LocalDateTime.now());
        taxRepository.save(tax);
        log.info("Tax soft-deleted: id={}", id);
    }

    private Tax findOrThrow(UUID id) {
        return taxRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Tax not found: " + id));
    }
}

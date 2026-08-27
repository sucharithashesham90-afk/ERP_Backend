package com.erp.platform.modules.sales.service;
import java.util.UUID;

import com.erp.platform.modules.sales.dto.AdvanceBookingSchemeDto;
import com.erp.platform.modules.sales.dto.CreateAdvanceBookingSchemeRequest;
import com.erp.platform.modules.sales.entity.AdvanceBookingScheme;
import com.erp.platform.modules.sales.repository.AdvanceBookingSchemeRepository;
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
public class AdvanceBookingSchemeService {

    private final AdvanceBookingSchemeRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<AdvanceBookingSchemeDto> list(int page, int size) {
        UUID tid = tenantContext.current();
        Page<AdvanceBookingScheme> p = repository.findByTenantIdAndDeletedAtIsNull(
                tid, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.map(this::toDto));
    }

    public AdvanceBookingSchemeDto create(CreateAdvanceBookingSchemeRequest req) {
        AdvanceBookingScheme e = new AdvanceBookingScheme();
        e.setTenantId(tenantContext.current());
        e.setSchemeCode(req.schemeCode());
        e.setSchemeName(req.schemeName());
        e.setFromDate(req.fromDate());
        e.setToDate(req.toDate());
        e.setProductName(req.productName());
        e.setBenefitType(req.benefitType());
        e.setFlatBenefitPercent(req.flatBenefitPercent());
        e.setMinOrderQty(req.minOrderQty());
        e.setDescription(req.description());
        e.setStatus(req.status() != null ? req.status() : "ACTIVE");
        return toDto(repository.save(e));
    }

    public AdvanceBookingSchemeDto update(UUID id, CreateAdvanceBookingSchemeRequest req) {
        AdvanceBookingScheme e = repository.findById(id).orElseThrow();
        e.setSchemeCode(req.schemeCode());
        e.setSchemeName(req.schemeName());
        e.setFromDate(req.fromDate());
        e.setToDate(req.toDate());
        e.setProductName(req.productName());
        e.setBenefitType(req.benefitType());
        e.setFlatBenefitPercent(req.flatBenefitPercent());
        e.setMinOrderQty(req.minOrderQty());
        e.setDescription(req.description());
        e.setStatus(req.status());
        return toDto(repository.save(e));
    }

    public void delete(UUID id) {
        AdvanceBookingScheme e = repository.findById(id).orElseThrow();
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private AdvanceBookingSchemeDto toDto(AdvanceBookingScheme e) {
        return new AdvanceBookingSchemeDto(
                e.getId(), e.getSchemeCode(), e.getSchemeName(),
                e.getFromDate(), e.getToDate(), e.getProductName(),
                e.getBenefitType(), e.getFlatBenefitPercent(),
                e.getMinOrderQty(), e.getDescription(), e.getStatus());
    }
}


package com.erp.platform.modules.sales.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.sales.dto.CreateExpectedSalesRequest;
import com.erp.platform.modules.sales.dto.ExpectedSalesDto;
import com.erp.platform.modules.sales.entity.ExpectedSales;
import com.erp.platform.modules.sales.repository.ExpectedSalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpectedSalesService {

    private final ExpectedSalesRepository expectedSalesRepository;
    private final TenantContext tenantContext;

    public PageResponse<ExpectedSalesDto> list(Pageable pageable) {
        return PageResponse.of(expectedSalesRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public ExpectedSalesDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        ExpectedSales entity = expectedSalesRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ExpectedSales not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public ExpectedSalesDto create(CreateExpectedSalesRequest request) {
        UUID tenantId = tenantContext.current();
        ExpectedSales entity = new ExpectedSales();
        entity.setTenantId(tenantId);
        entity.setCropGroup(request.getCropGroup());
        entity.setCropName(request.getCropName());
        entity.setVarietyName(request.getVarietyName());
        entity.setSalesArea(request.getSalesArea());
        entity.setSalesPeriod(request.getSalesPeriod());
        entity.setFromDate(request.getFromDate());
        entity.setToDate(request.getToDate());
        entity.setExpectedSalesKgs(request.getExpectedSalesKgs());
        entity.setExpectedDealerBalanceKgs(request.getExpectedDealerBalanceKgs());
        entity.setRemarks(request.getRemarks());
        return toDto(expectedSalesRepository.save(entity));
    }

    @Transactional
    public ExpectedSalesDto update(UUID id, CreateExpectedSalesRequest request) {
        UUID tenantId = tenantContext.current();
        ExpectedSales entity = expectedSalesRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ExpectedSales not found: " + id));
        entity.setCropGroup(request.getCropGroup());
        entity.setCropName(request.getCropName());
        entity.setVarietyName(request.getVarietyName());
        entity.setSalesArea(request.getSalesArea());
        entity.setSalesPeriod(request.getSalesPeriod());
        entity.setFromDate(request.getFromDate());
        entity.setToDate(request.getToDate());
        entity.setExpectedSalesKgs(request.getExpectedSalesKgs());
        entity.setExpectedDealerBalanceKgs(request.getExpectedDealerBalanceKgs());
        entity.setRemarks(request.getRemarks());
        return toDto(expectedSalesRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        ExpectedSales entity = expectedSalesRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("ExpectedSales not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        expectedSalesRepository.save(entity);
    }

    private ExpectedSalesDto toDto(ExpectedSales entity) {
        ExpectedSalesDto dto = new ExpectedSalesDto();
        dto.setId(entity.getId());
        dto.setCropGroup(entity.getCropGroup());
        dto.setCropName(entity.getCropName());
        dto.setVarietyName(entity.getVarietyName());
        dto.setSalesArea(entity.getSalesArea());
        dto.setSalesPeriod(entity.getSalesPeriod());
        dto.setFromDate(entity.getFromDate());
        dto.setToDate(entity.getToDate());
        dto.setExpectedSalesKgs(entity.getExpectedSalesKgs());
        dto.setExpectedDealerBalanceKgs(entity.getExpectedDealerBalanceKgs());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}


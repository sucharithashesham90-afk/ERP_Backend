package com.erp.platform.modules.hr.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.dto.CreatePayGradeRequest;
import com.erp.platform.modules.hr.dto.PayGradeDto;
import com.erp.platform.modules.hr.entity.PayGrade;
import com.erp.platform.modules.hr.repository.PayGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayGradeService {

    private final PayGradeRepository payGradeRepository;
    private final TenantContext tenantContext;

    public PageResponse<PayGradeDto> list(Pageable pageable) {
        return PageResponse.of(payGradeRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public PayGradeDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        PayGrade entity = payGradeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("PayGrade not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public PayGradeDto create(CreatePayGradeRequest request) {
        UUID tenantId = tenantContext.current();
        PayGrade entity = new PayGrade();
        entity.setTenantId(tenantId);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setMinSalary(request.getMinSalary());
        entity.setMaxSalary(request.getMaxSalary());
        entity.setProvidentFundPercent(request.getProvidentFundPercent());
        entity.setOtPremiumFactor(request.getOtPremiumFactor());
        entity.setLossOfPayApplicable(request.isLossOfPayApplicable());
        entity.setOverTimeApplicable(request.isOverTimeApplicable());
        entity.setActive(request.isActive());
        return toDto(payGradeRepository.save(entity));
    }

    @Transactional
    public PayGradeDto update(UUID id, CreatePayGradeRequest request) {
        UUID tenantId = tenantContext.current();
        PayGrade entity = payGradeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("PayGrade not found: " + id));
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setMinSalary(request.getMinSalary());
        entity.setMaxSalary(request.getMaxSalary());
        entity.setProvidentFundPercent(request.getProvidentFundPercent());
        entity.setOtPremiumFactor(request.getOtPremiumFactor());
        entity.setLossOfPayApplicable(request.isLossOfPayApplicable());
        entity.setOverTimeApplicable(request.isOverTimeApplicable());
        entity.setActive(request.isActive());
        return toDto(payGradeRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        PayGrade entity = payGradeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("PayGrade not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        payGradeRepository.save(entity);
    }

    private PayGradeDto toDto(PayGrade entity) {
        PayGradeDto dto = new PayGradeDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setMinSalary(entity.getMinSalary());
        dto.setMaxSalary(entity.getMaxSalary());
        dto.setProvidentFundPercent(entity.getProvidentFundPercent());
        dto.setOtPremiumFactor(entity.getOtPremiumFactor());
        dto.setLossOfPayApplicable(entity.isLossOfPayApplicable());
        dto.setOverTimeApplicable(entity.isOverTimeApplicable());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}


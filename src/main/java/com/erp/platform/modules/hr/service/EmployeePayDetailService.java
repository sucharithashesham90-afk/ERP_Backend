package com.erp.platform.modules.hr.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.dto.CreateEmployeePayDetailRequest;
import com.erp.platform.modules.hr.dto.EmployeePayDetailDto;
import com.erp.platform.modules.hr.entity.EmployeePayDetail;
import com.erp.platform.modules.hr.repository.EmployeePayDetailRepository;
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
public class EmployeePayDetailService {

    private final EmployeePayDetailRepository employeePayDetailRepository;
    private final HrAccessService hrAccess;
    private final TenantContext tenantContext;

    public PageResponse<EmployeePayDetailDto> list(Pageable pageable) {
        if (!hrAccess.isHr()) {
            return PageResponse.of(employeePayDetailRepository
                    .findOwnedBy(tenantContext.current(), hrAccess.currentEmployeeKeys(), pageable).map(this::toDto));
        }
        return PageResponse.of(employeePayDetailRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public EmployeePayDetailDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        EmployeePayDetail entity = employeePayDetailRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("EmployeePayDetail not found: " + id));
        hrAccess.assertCanSeeKey(entity.getEmployeeId());
        return toDto(entity);
    }

    @Transactional
    public EmployeePayDetailDto create(CreateEmployeePayDetailRequest request) {
        hrAccess.assertHr();  // nobody sets their own salary
        UUID tenantId = tenantContext.current();
        EmployeePayDetail entity = new EmployeePayDetail();
        entity.setTenantId(tenantId);
        entity.setEmployeeId(request.getEmployeeId());
        entity.setEmployeeName(request.getEmployeeName());
        entity.setModeOfSalaryPayment(request.getModeOfSalaryPayment());
        entity.setSalaryDay(request.getSalaryDay());
        entity.setEmployeeStartingDate(request.getEmployeeStartingDate());
        entity.setGrossSalary(request.getGrossSalary());
        entity.setProvidentFundPercent(request.getProvidentFundPercent());
        entity.setOtPremiumFactor(request.getOtPremiumFactor());
        entity.setLossOfPayApplicable(request.isLossOfPayApplicable());
        entity.setOverTimeApplicable(request.isOverTimeApplicable());
        entity.setPayGradeId(request.getPayGradeId());
        return toDto(employeePayDetailRepository.save(entity));
    }

    @Transactional
    public EmployeePayDetailDto update(UUID id, CreateEmployeePayDetailRequest request) {
        hrAccess.assertHr();  // nobody edits their own salary
        UUID tenantId = tenantContext.current();
        EmployeePayDetail entity = employeePayDetailRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("EmployeePayDetail not found: " + id));
        entity.setEmployeeId(request.getEmployeeId());
        entity.setEmployeeName(request.getEmployeeName());
        entity.setModeOfSalaryPayment(request.getModeOfSalaryPayment());
        entity.setSalaryDay(request.getSalaryDay());
        entity.setEmployeeStartingDate(request.getEmployeeStartingDate());
        entity.setGrossSalary(request.getGrossSalary());
        entity.setProvidentFundPercent(request.getProvidentFundPercent());
        entity.setOtPremiumFactor(request.getOtPremiumFactor());
        entity.setLossOfPayApplicable(request.isLossOfPayApplicable());
        entity.setOverTimeApplicable(request.isOverTimeApplicable());
        entity.setPayGradeId(request.getPayGradeId());
        return toDto(employeePayDetailRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        hrAccess.assertHr();
        UUID tenantId = tenantContext.current();
        EmployeePayDetail entity = employeePayDetailRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("EmployeePayDetail not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        employeePayDetailRepository.save(entity);
    }

    private EmployeePayDetailDto toDto(EmployeePayDetail entity) {
        EmployeePayDetailDto dto = new EmployeePayDetailDto();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setEmployeeName(entity.getEmployeeName());
        dto.setModeOfSalaryPayment(entity.getModeOfSalaryPayment());
        dto.setSalaryDay(entity.getSalaryDay());
        dto.setEmployeeStartingDate(entity.getEmployeeStartingDate());
        dto.setGrossSalary(entity.getGrossSalary());
        dto.setProvidentFundPercent(entity.getProvidentFundPercent());
        dto.setOtPremiumFactor(entity.getOtPremiumFactor());
        dto.setLossOfPayApplicable(entity.isLossOfPayApplicable());
        dto.setOverTimeApplicable(entity.isOverTimeApplicable());
        dto.setPayGradeId(entity.getPayGradeId());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}


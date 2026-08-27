package com.erp.platform.modules.hr.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.dto.CreateTadaClaimRequest;
import com.erp.platform.modules.hr.dto.TadaClaimDto;
import com.erp.platform.modules.hr.entity.TadaClaim;
import com.erp.platform.modules.hr.repository.TadaClaimRepository;
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
public class TadaClaimService {

    private final TadaClaimRepository tadaClaimRepository;
    private final HrAccessService hrAccess;
    private final TenantContext tenantContext;

    public PageResponse<TadaClaimDto> list(Pageable pageable) {
        if (!hrAccess.isHr()) {
            return PageResponse.of(tadaClaimRepository
                    .findOwnedBy(tenantContext.current(), hrAccess.currentEmployeeKeys(), pageable).map(this::toDto));
        }
        return PageResponse.of(tadaClaimRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public TadaClaimDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        TadaClaim entity = tadaClaimRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("TadaClaim not found: " + id));
        hrAccess.assertCanSeeKey(entity.getEmployeeId());
        return toDto(entity);
    }

    @Transactional
    public TadaClaimDto create(CreateTadaClaimRequest request) {
        UUID tenantId = tenantContext.current();
        TadaClaim entity = new TadaClaim();
        entity.setTenantId(tenantId);
        entity.setEmployeeId(request.getEmployeeId());
        entity.setEmployeeName(request.getEmployeeName());
        entity.setClaimDate(request.getClaimDate());
        entity.setTravelType(request.getTravelType());
        entity.setFromPlace(request.getFromPlace());
        entity.setToPlace(request.getToPlace());
        entity.setTravelDays(request.getTravelDays());
        entity.setDailyAllowanceRate(request.getDailyAllowanceRate());
        entity.setTravelAllowance(request.getTravelAllowance());
        entity.setTotalAmount(request.getTotalAmount());
        entity.setStatus(request.getStatus());
        entity.setRemarks(request.getRemarks());
        return toDto(tadaClaimRepository.save(entity));
    }

    @Transactional
    public TadaClaimDto update(UUID id, CreateTadaClaimRequest request) {
        UUID tenantId = tenantContext.current();
        TadaClaim entity = tadaClaimRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("TadaClaim not found: " + id));
        entity.setEmployeeId(request.getEmployeeId());
        entity.setEmployeeName(request.getEmployeeName());
        entity.setClaimDate(request.getClaimDate());
        entity.setTravelType(request.getTravelType());
        entity.setFromPlace(request.getFromPlace());
        entity.setToPlace(request.getToPlace());
        entity.setTravelDays(request.getTravelDays());
        entity.setDailyAllowanceRate(request.getDailyAllowanceRate());
        entity.setTravelAllowance(request.getTravelAllowance());
        entity.setTotalAmount(request.getTotalAmount());
        entity.setStatus(request.getStatus());
        entity.setRemarks(request.getRemarks());
        return toDto(tadaClaimRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        TadaClaim entity = tadaClaimRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("TadaClaim not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        tadaClaimRepository.save(entity);
    }

    private TadaClaimDto toDto(TadaClaim entity) {
        TadaClaimDto dto = new TadaClaimDto();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setEmployeeName(entity.getEmployeeName());
        dto.setClaimDate(entity.getClaimDate());
        dto.setTravelType(entity.getTravelType());
        dto.setFromPlace(entity.getFromPlace());
        dto.setToPlace(entity.getToPlace());
        dto.setTravelDays(entity.getTravelDays());
        dto.setDailyAllowanceRate(entity.getDailyAllowanceRate());
        dto.setTravelAllowance(entity.getTravelAllowance());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setStatus(entity.getStatus());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}


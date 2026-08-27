package com.erp.platform.modules.shareholder.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.shareholder.dto.ShareHolderDto;
import com.erp.platform.modules.shareholder.entity.ShareHolder;
import com.erp.platform.modules.shareholder.repository.ShareHolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShareHolderService {

    private final ShareHolderRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<ShareHolderDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public ShareHolderDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ShareHolderDto create(ShareHolderDto req) {
        UUID tenantId = tenantContext.current();
        ShareHolder entity = new ShareHolder();
        entity.setTenantId(tenantId);
        entity.setShareholderNumber(req.getShareholderNumber() != null && !req.getShareholderNumber().isBlank()
                ? req.getShareholderNumber() : generateNumber(tenantId));
        applyFields(entity, req);
        entity = repository.save(entity);
        log.info("ShareHolder created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ShareHolderDto update(UUID id, ShareHolderDto req) {
        ShareHolder entity = findOrThrow(id);
        applyFields(entity, req);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ShareHolder entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyFields(ShareHolder e, ShareHolderDto req) {
        e.setName(req.getName());
        e.setAddress(req.getAddress());
        e.setEmail(req.getEmail());
        e.setPhone(req.getPhone());
        e.setPanNumber(req.getPanNumber());
        e.setAadharNumber(req.getAadharNumber());
        e.setSharesHeld(req.getSharesHeld());
        e.setFaceValuePerShare(req.getFaceValuePerShare());
        e.setDateOfAllotment(req.getDateOfAllotment());
        e.setNomineeName(req.getNomineeName());
        e.setNomineeRelationship(req.getNomineeRelationship());
        e.setStatus(req.getStatus() != null ? req.getStatus() : "ACTIVE");
        e.setRemarks(req.getRemarks());
    }

    private ShareHolder findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Shareholder not found: " + id));
    }

    private ShareHolderDto toDto(ShareHolder e) {
        ShareHolderDto dto = new ShareHolderDto();
        dto.setId(e.getId());
        dto.setShareholderNumber(e.getShareholderNumber());
        dto.setName(e.getName());
        dto.setAddress(e.getAddress());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setPanNumber(e.getPanNumber());
        dto.setAadharNumber(e.getAadharNumber());
        dto.setSharesHeld(e.getSharesHeld());
        dto.setFaceValuePerShare(e.getFaceValuePerShare());
        dto.setDateOfAllotment(e.getDateOfAllotment());
        dto.setNomineeName(e.getNomineeName());
        dto.setNomineeRelationship(e.getNomineeRelationship());
        dto.setStatus(e.getStatus());
        dto.setRemarks(e.getRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private String generateNumber(UUID tenantId) {
        long count = repository.countByTenantId(tenantId);
        return "SH-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + String.format("%04d", count + 1);
    }
}

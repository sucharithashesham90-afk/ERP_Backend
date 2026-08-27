package com.erp.platform.modules.shareholder.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.shareholder.dto.ShareTransferDto;
import com.erp.platform.modules.shareholder.entity.ShareHolder;
import com.erp.platform.modules.shareholder.entity.ShareTransfer;
import com.erp.platform.modules.shareholder.repository.ShareHolderRepository;
import com.erp.platform.modules.shareholder.repository.ShareTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShareTransferService {

    private final ShareTransferRepository repository;
    private final ShareHolderRepository holderRepository;
    private final TenantContext tenantContext;

    public PageResponse<ShareTransferDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public ShareTransferDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ShareTransferDto create(ShareTransferDto req) {
        UUID tenantId = tenantContext.current();
        ShareTransfer entity = new ShareTransfer();
        entity.setTenantId(tenantId);
        entity.setTransferNumber(generateNumber());
        applyFields(entity, req, tenantId);
        entity = repository.save(entity);
        log.info("ShareTransfer created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ShareTransferDto approve(UUID id) {
        UUID tenantId = tenantContext.current();
        ShareTransfer transfer = findOrThrow(id);
        if (!"PENDING".equals(transfer.getStatus())) {
            throw AppException.badRequest("Only PENDING transfers can be approved");
        }
        BigDecimal shares = transfer.getSharesTransferred() != null ? transfer.getSharesTransferred() : BigDecimal.ZERO;

        if (transfer.getFromShareholderId() != null) {
            holderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, transfer.getFromShareholderId())
                    .ifPresent(from -> {
                        BigDecimal current = from.getSharesHeld() != null ? from.getSharesHeld() : BigDecimal.ZERO;
                        if (current.compareTo(shares) < 0) {
                            throw AppException.badRequest("Insufficient shares: holder has " + current + ", transfer requires " + shares);
                        }
                        from.setSharesHeld(current.subtract(shares));
                        holderRepository.save(from);
                    });
        }
        if (transfer.getToShareholderId() != null) {
            holderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, transfer.getToShareholderId())
                    .ifPresent(to -> {
                        BigDecimal current = to.getSharesHeld() != null ? to.getSharesHeld() : BigDecimal.ZERO;
                        to.setSharesHeld(current.add(shares));
                        holderRepository.save(to);
                    });
        }

        transfer.setStatus("APPROVED");
        transfer = repository.save(transfer);
        log.info("ShareTransfer approved: {}", id);
        return toDto(transfer);
    }

    @Transactional
    public ShareTransferDto update(UUID id, ShareTransferDto req) {
        ShareTransfer entity = findOrThrow(id);
        if ("APPROVED".equals(entity.getStatus())) {
            throw AppException.badRequest("Approved transfers cannot be modified");
        }
        applyFields(entity, req, tenantContext.current());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ShareTransfer entity = findOrThrow(id);
        if ("APPROVED".equals(entity.getStatus())) {
            throw AppException.badRequest("Approved transfers cannot be deleted");
        }
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyFields(ShareTransfer e, ShareTransferDto req, UUID tenantId) {
        e.setFromShareholderId(req.getFromShareholderId());
        e.setToShareholderId(req.getToShareholderId());
        e.setTransferDate(req.getTransferDate() != null ? req.getTransferDate() : LocalDate.now());
        e.setSharesTransferred(req.getSharesTransferred());
        e.setTransferPricePerShare(req.getTransferPricePerShare());
        e.setStampDuty(req.getStampDuty());
        e.setTransferType(req.getTransferType());
        e.setInstrumentNumber(req.getInstrumentNumber());
        e.setStatus(req.getStatus() != null ? req.getStatus() : "PENDING");
        e.setRemarks(req.getRemarks());

        if (req.getFromShareholderId() != null) {
            holderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getFromShareholderId())
                    .ifPresent(h -> e.setFromShareholderName(h.getName()));
        }
        if (req.getToShareholderId() != null) {
            holderRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getToShareholderId())
                    .ifPresent(h -> e.setToShareholderName(h.getName()));
        }
    }

    private ShareTransfer findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Share transfer not found: " + id));
    }

    private ShareTransferDto toDto(ShareTransfer e) {
        ShareTransferDto dto = new ShareTransferDto();
        dto.setId(e.getId());
        dto.setTransferNumber(e.getTransferNumber());
        dto.setFromShareholderId(e.getFromShareholderId());
        dto.setFromShareholderName(e.getFromShareholderName());
        dto.setToShareholderId(e.getToShareholderId());
        dto.setToShareholderName(e.getToShareholderName());
        dto.setTransferDate(e.getTransferDate());
        dto.setSharesTransferred(e.getSharesTransferred());
        dto.setTransferPricePerShare(e.getTransferPricePerShare());
        dto.setStampDuty(e.getStampDuty());
        dto.setTransferType(e.getTransferType());
        dto.setInstrumentNumber(e.getInstrumentNumber());
        dto.setStatus(e.getStatus());
        dto.setRemarks(e.getRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private String generateNumber() {
        return "ST-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

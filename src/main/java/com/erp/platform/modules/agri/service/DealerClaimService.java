package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateDealerClaimRequest;
import com.erp.platform.modules.agri.dto.DealerClaimDto;
import com.erp.platform.modules.agri.entity.DealerClaim;
import com.erp.platform.modules.agri.repository.DealerClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DealerClaimService {

    private final DealerClaimRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<DealerClaimDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public DealerClaimDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public DealerClaimDto create(CreateDealerClaimRequest request) {
        UUID tenantId = tenantContext.current();
        DealerClaim entity = new DealerClaim();
        entity.setTenantId(tenantId);
        entity.setClaimNumber(request.claimNumber());
        entity.setClaimDate(request.claimDate());
        entity.setDealerName(request.dealerName());
        entity.setSchemeName(request.schemeName());
        entity.setProductName(request.productName());
        entity.setQuantitySold(request.quantitySold());
        entity.setClaimAmountPerUnit(request.claimAmountPerUnit());
        entity.setTotalClaim(request.totalClaim());
        entity.setSupportingDocsRef(request.supportingDocsRef());
        entity.setStatus(request.status() != null ? request.status() : "PENDING");
        entity = repository.save(entity);
        log.info("DealerClaim created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public DealerClaimDto update(UUID id, CreateDealerClaimRequest request) {
        DealerClaim entity = findOrThrow(id);
        entity.setClaimNumber(request.claimNumber());
        entity.setClaimDate(request.claimDate());
        entity.setDealerName(request.dealerName());
        entity.setSchemeName(request.schemeName());
        entity.setProductName(request.productName());
        entity.setQuantitySold(request.quantitySold());
        entity.setClaimAmountPerUnit(request.claimAmountPerUnit());
        entity.setTotalClaim(request.totalClaim());
        entity.setSupportingDocsRef(request.supportingDocsRef());
        entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        DealerClaim entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private DealerClaim findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("DealerClaim not found: " + id));
    }

    private DealerClaimDto toDto(DealerClaim e) {
        return new DealerClaimDto(
                e.getId(),
                e.getClaimNumber(),
                e.getClaimDate(),
                e.getDealerName(),
                e.getSchemeName(),
                e.getProductName(),
                e.getQuantitySold(),
                e.getClaimAmountPerUnit(),
                e.getTotalClaim(),
                e.getSupportingDocsRef(),
                e.getStatus()
        );
    }
}

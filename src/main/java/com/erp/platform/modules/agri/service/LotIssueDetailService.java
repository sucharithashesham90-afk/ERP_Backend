package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateLotIssueDetailRequest;
import com.erp.platform.modules.agri.dto.LotIssueDetailDto;
import com.erp.platform.modules.agri.entity.LotIssueDetail;
import com.erp.platform.modules.agri.repository.LotIssueDetailRepository;
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
public class LotIssueDetailService {

    private final LotIssueDetailRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<LotIssueDetailDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public LotIssueDetailDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public LotIssueDetailDto create(CreateLotIssueDetailRequest request) {
        UUID tenantId = tenantContext.current();
        LotIssueDetail entity = new LotIssueDetail();
        entity.setTenantId(tenantId);
        entity.setIssueNumber(request.issueNumber());
        entity.setIssueDate(request.issueDate());
        entity.setLotNumber(request.lotNumber());
        entity.setLocation(request.location());
        entity.setGodown(request.godown());
        entity.setMaterialGroup(request.materialGroup());
        entity.setMaterialItem(request.materialItem());
        entity.setQuantity(request.quantity());
        entity.setUnit(request.unit());
        entity.setIssueType(request.issueType() != null ? request.issueType() : "PROCESSING");
        entity.setRemarks(request.remarks());
        entity = repository.save(entity);
        log.info("LotIssueDetail created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public LotIssueDetailDto update(UUID id, CreateLotIssueDetailRequest request) {
        LotIssueDetail entity = findOrThrow(id);
        entity.setIssueNumber(request.issueNumber());
        entity.setIssueDate(request.issueDate());
        entity.setLotNumber(request.lotNumber());
        entity.setLocation(request.location());
        entity.setGodown(request.godown());
        entity.setMaterialGroup(request.materialGroup());
        entity.setMaterialItem(request.materialItem());
        entity.setQuantity(request.quantity());
        entity.setUnit(request.unit());
        entity.setIssueType(request.issueType());
        entity.setRemarks(request.remarks());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        LotIssueDetail entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private LotIssueDetail findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("LotIssueDetail not found: " + id));
    }

    private LotIssueDetailDto toDto(LotIssueDetail e) {
        return new LotIssueDetailDto(
                e.getId(),
                e.getIssueNumber(),
                e.getIssueDate(),
                e.getLotNumber(),
                e.getLocation(),
                e.getGodown(),
                e.getMaterialGroup(),
                e.getMaterialItem(),
                e.getQuantity(),
                e.getUnit(),
                e.getIssueType(),
                e.getRemarks()
        );
    }
}

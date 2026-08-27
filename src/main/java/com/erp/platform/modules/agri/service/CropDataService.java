package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateCropDataRequest;
import com.erp.platform.modules.agri.dto.CropDataDto;
import com.erp.platform.modules.agri.entity.CropData;
import com.erp.platform.modules.agri.repository.CropDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CropDataService {

    private final CropDataRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<CropDataDto> findAll(Pageable pageable) {
        return findAll(null, pageable);
    }

    /** List crops, optionally narrowed to a crop group (drives the crop-group → crop cascade). */
    public PageResponse<CropDataDto> findAll(UUID cropGroupId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (cropGroupId == null) {
            return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
        }
        var all = repository.findByTenantIdAndDeletedAtIsNull(tenantId).stream()
                .filter(c -> cropGroupId.equals(c.getCropGroupId()))
                .map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        var page = start > all.size() ? java.util.List.<CropDataDto>of() : all.subList(start, end);
        return PageResponse.of(new org.springframework.data.domain.PageImpl<>(page, pageable, all.size()));
    }

    /**
     * Every crop, or just those in one group.
     *
     * <p>The cascade screens have always passed a cropGroupId here and it was quietly ignored, so
     * choosing a crop group still offered every crop in the business. A filter that is accepted
     * and discarded is worse than one that is rejected — the screen looks like it is working.
     */
    public List<CropDataDto> findAllList(UUID cropGroupId) {
        return repository.findByTenantIdAndDeletedAtIsNull(tenantContext.current())
                .stream()
                .filter(c -> cropGroupId == null || cropGroupId.equals(c.getCropGroupId()))
                .map(this::toDto).toList();
    }

    public List<CropDataDto> findAllList() {
        return findAllList(null);
    }

    public CropDataDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public CropDataDto create(CreateCropDataRequest request) {
        CropData entity = new CropData();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, request);
        entity = repository.save(entity);
        log.info("CropData created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public CropDataDto update(UUID id, CreateCropDataRequest request) {
        CropData entity = findOrThrow(id);
        applyRequest(entity, request);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        CropData entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(CropData e, CreateCropDataRequest r) {
        e.setCropName(r.cropName());
        e.setCropCode(r.cropCode());
        e.setCropGroupId(r.cropGroupId());
        e.setCropGroupName(r.cropGroupName());
        e.setSeasonIds(listToCsv(r.seasonIds()));
        e.setSeasonNames(r.seasonNames());
        e.setNoOfInspections(r.noOfInspections());
        e.setUom(r.uom());
        // Automatic Sample Creation
        e.setSampleAtIntake(r.sampleAtIntake());
        e.setSampleAtSalesReturn(r.sampleAtSalesReturn());
        e.setSampleAtThirdParty(r.sampleAtThirdParty());
        e.setSampleAtSeedExpire(r.sampleAtSeedExpire());
        // Timing & Sizing
        e.setCertificationTime(r.certificationTime());
        e.setCertificationTimeUnit(r.certificationTimeUnit());
        e.setProcessingTime(r.processingTime());
        e.setProcessingTimeUnit(r.processingTimeUnit());
        e.setLotSizeQty(r.lotSizeQty());
        e.setIsolationDistance(r.isolationDistance());
        // Parent Seed Pack
        e.setParentSeedPackType(r.parentSeedPackType());
        e.setParentSeedPackSizeKgs(r.parentSeedPackSizeKgs());
        // Material
        e.setMaterialState(r.materialState());
        e.setProductName(r.productName());
        e.setMaterialType(r.materialType());
        // General
        e.setNotes(r.notes());
    }

    private CropData findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("CropData not found: " + id));
    }

    private CropDataDto toDto(CropData e) {
        return new CropDataDto(
                e.getId(),
                e.getCropName(),
                e.getCropCode(),
                e.getCropGroupId(),
                e.getCropGroupName(),
                csvToList(e.getSeasonIds()),
                e.getSeasonNames(),
                e.getNoOfInspections(),
                e.getUom(),
                e.isSampleAtIntake(),
                e.isSampleAtSalesReturn(),
                e.isSampleAtThirdParty(),
                e.isSampleAtSeedExpire(),
                e.getCertificationTime(),
                e.getCertificationTimeUnit(),
                e.getProcessingTime(),
                e.getProcessingTimeUnit(),
                e.getLotSizeQty(),
                e.getIsolationDistance(),
                e.getParentSeedPackType(),
                e.getParentSeedPackSizeKgs(),
                e.getMaterialState(),
                e.getProductName(),
                e.getMaterialType(),
                e.getNotes(),
                e.getCreatedAt()
        );
    }

    private String listToCsv(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    private List<String> csvToList(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.asList(csv.split(","));
    }
}

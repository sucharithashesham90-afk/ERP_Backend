package com.erp.platform.modules.quality.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.quality.dto.CreatePublishResultRequest;
import com.erp.platform.modules.quality.dto.PublishResultDto;
import com.erp.platform.modules.quality.entity.PublishResult;
import com.erp.platform.modules.quality.entity.Sample;
import com.erp.platform.modules.quality.repository.PublishResultRepository;
import com.erp.platform.modules.quality.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PublishResultService {

    private final PublishResultRepository repository;
    private final SampleRepository sampleRepository;
    private final TenantContext tenantContext;

    public PageResponse<PublishResultDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public PublishResultDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PublishResultDto create(CreatePublishResultRequest request) {
        UUID tenantId = tenantContext.current();
        PublishResult entity = new PublishResult();
        entity.setTenantId(tenantId);
        String pubNo = request.publishNumber();
        if (pubNo == null || pubNo.isBlank()) {
            long n = repository.findByTenantIdAndDeletedAtIsNull(tenantId, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
            pubNo = String.format("PUB-%d-%04d", java.time.LocalDate.now().getYear(), n + 1);
        }
        entity.setPublishNumber(pubNo);
        entity.setPublishDate(request.publishDate());
        entity.setLotNumber(request.lotNumber());
        entity.setTestResultNumber(request.testResultNumber());
        entity.setPublishedBy(request.publishedBy());
        entity.setPublicationNote(request.publicationNote());
        entity.setPublishStatus(request.publishStatus() != null ? request.publishStatus() : "DRAFT");
        entity = repository.save(entity);
        log.info("PublishResult created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public PublishResultDto update(UUID id, CreatePublishResultRequest request) {
        PublishResult entity = findOrThrow(id);
        if (request.publishNumber() != null && !request.publishNumber().isBlank()) entity.setPublishNumber(request.publishNumber());
        entity.setPublishDate(request.publishDate());
        entity.setLotNumber(request.lotNumber());
        entity.setTestResultNumber(request.testResultNumber());
        entity.setPublishedBy(request.publishedBy());
        entity.setPublicationNote(request.publicationNote());
        entity.setPublishStatus(request.publishStatus());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PublishResult entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    /**
     * The only way a PublishResult row is created now — Result Entry selects graded samples and
     * publishes them in bulk. One row per sample, upserted so re-publishing an already-published
     * sample just refreshes it instead of creating a duplicate.
     */
    @Transactional
    public List<PublishResultDto> publishSamples(List<UUID> sampleIds) {
        if (sampleIds == null || sampleIds.isEmpty()) {
            throw AppException.badRequest("No samples selected to publish");
        }
        UUID tenantId = tenantContext.current();
        List<Sample> samples = sampleRepository.findByTenantIdAndIdInAndDeletedAtIsNull(tenantId, sampleIds);
        if (samples.size() != sampleIds.size()) {
            throw AppException.notFound("One or more selected samples were not found");
        }
        Map<UUID, PublishResult> existingBySample = repository
                .findByTenantIdAndSampleIdInAndDeletedAtIsNull(tenantId, sampleIds).stream()
                .collect(Collectors.toMap(PublishResult::getSampleId, r -> r));

        String publishedBy = currentUsername();
        long startingCount = repository.findByTenantIdAndDeletedAtIsNull(tenantId, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();

        List<PublishResult> saved = new java.util.ArrayList<>();
        int i = 0;
        for (Sample sample : samples) {
            if (sample.getResultStatus() == null || sample.getResultStatus().isBlank()) {
                throw AppException.badRequest("Sample " + sample.getSampleNumber() + " has no result entered yet — record a Pass/Fail result before publishing");
            }
            PublishResult entity = existingBySample.get(sample.getId());
            if (entity == null) {
                entity = new PublishResult();
                entity.setTenantId(tenantId);
                entity.setSampleId(sample.getId());
                entity.setPublishNumber(String.format("PUB-%d-%04d", LocalDate.now().getYear(), startingCount + (++i)));
            }
            entity.setPublishDate(LocalDate.now());
            entity.setLotNumber(sample.getLotNumber());
            entity.setTestResultNumber(sample.getSampleNumber());
            entity.setPublishedBy(publishedBy);
            entity.setPublishStatus("PUBLISHED");
            saved.add(repository.save(entity));
        }
        log.info("Bulk-published {} sample results", saved.size());
        return saved.stream().map(this::toDto).toList();
    }

    private String currentUsername() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private PublishResult findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("PublishResult not found: " + id));
    }

    private PublishResultDto toDto(PublishResult e) {
        return new PublishResultDto(
                e.getId(),
                e.getPublishNumber(),
                e.getPublishDate(),
                e.getLotNumber(),
                e.getTestResultNumber(),
                e.getPublishedBy(),
                e.getPublicationNote(),
                e.getPublishStatus(),
                e.getSampleId()
        );
    }
}

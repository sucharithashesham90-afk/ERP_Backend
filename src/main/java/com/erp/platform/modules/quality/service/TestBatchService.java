package com.erp.platform.modules.quality.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.quality.dto.CreateTestBatchRequest;
import com.erp.platform.modules.quality.dto.TestBatchDto;
import com.erp.platform.modules.quality.entity.Sample;
import com.erp.platform.modules.quality.entity.TestBatch;
import com.erp.platform.modules.quality.entity.TestBatch.TestResult;
import com.erp.platform.modules.quality.entity.TestBatch.TestStatus;
import com.erp.platform.modules.quality.entity.TestDefinition;
import com.erp.platform.modules.quality.entity.TestParameter;
import com.erp.platform.modules.quality.entity.TestResultDetail;
import com.erp.platform.modules.quality.entity.TestResultDetail.ParameterResult;
import com.erp.platform.modules.quality.repository.SampleRepository;
import com.erp.platform.modules.quality.repository.TestBatchRepository;
import com.erp.platform.modules.quality.repository.TestDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TestBatchService {

    private final TestBatchRepository testBatchRepository;
    private final TestDefinitionRepository testDefinitionRepository;
    private final SampleRepository sampleRepository;
    private final TenantContext tenantContext;

    public PageResponse<TestBatchDto> list(TestStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? testBatchRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : testBatchRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public TestBatchDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public TestBatchDto create(CreateTestBatchRequest request) {
        UUID tenantId = tenantContext.current();

        TestDefinition definition = testDefinitionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getDefinitionId())
                .orElseThrow(() -> AppException.notFound("Test definition not found: " + request.getDefinitionId()));

        Sample sample = sampleRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getSampleId())
                .orElseThrow(() -> AppException.notFound("Sample not found: " + request.getSampleId()));

        TestBatch batch = new TestBatch();
        batch.setTenantId(tenantId);
        batch.setBatchNumber(generateBatchNumber(tenantId));
        batch.setDefinition(definition);
        batch.setSample(sample);
        batch.setTestDate(request.getTestDate() != null ? request.getTestDate() : LocalDate.now());
        batch.setTestedBy(request.getTestedBy());
        batch.setLabLocation(request.getLabLocation());
        batch.setStatus(TestStatus.PENDING);
        batch.setOverallResult(TestResult.PENDING);
        batch.setNotes(request.getNotes());

        List<TestResultDetail> results = buildResults(batch, request.getResults(), tenantId);
        batch.setResults(results);

        TestBatch saved = testBatchRepository.save(batch);
        log.info("TestBatch created: id={}, number={}", saved.getId(), saved.getBatchNumber());
        return toDto(saved);
    }

    @Transactional
    public TestBatchDto updateStatus(UUID id, TestStatus status) {
        TestBatch batch = findOrThrow(id);
        batch.setStatus(status);
        return toDto(testBatchRepository.save(batch));
    }

    @Transactional
    public TestBatchDto complete(UUID id) {
        TestBatch batch = findOrThrow(id);
        if (batch.getStatus() == TestStatus.CANCELLED) {
            throw AppException.badRequest("Cannot complete a cancelled test batch");
        }
        batch.setStatus(TestStatus.COMPLETED);

        boolean anyFail = batch.getResults() != null && batch.getResults().stream()
                .anyMatch(r -> r.getResult() == ParameterResult.FAIL);
        batch.setOverallResult(anyFail ? TestResult.FAIL : TestResult.PASS);

        return toDto(testBatchRepository.save(batch));
    }

    @Transactional
    public TestBatchDto updateResults(UUID id, List<CreateTestBatchRequest.ResultDetailRequest> resultRequests) {
        UUID tenantId = tenantContext.current();
        TestBatch batch = findOrThrow(id);
        batch.getResults().clear();
        List<TestResultDetail> results = buildResults(batch, resultRequests, tenantId);
        batch.getResults().addAll(results);
        return toDto(testBatchRepository.save(batch));
    }

    @Transactional
    public void delete(UUID id) {
        TestBatch batch = findOrThrow(id);
        batch.setDeletedAt(LocalDateTime.now());
        testBatchRepository.save(batch);
        log.info("TestBatch soft-deleted: id={}", id);
    }

    private List<TestResultDetail> buildResults(TestBatch batch,
            List<CreateTestBatchRequest.ResultDetailRequest> requests, UUID tenantId) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            TestResultDetail detail = new TestResultDetail();
            detail.setTenantId(tenantId);
            detail.setTestBatch(batch);
            if (r.getParameterId() != null) {
                TestParameter param = new TestParameter();
                param.setId(r.getParameterId());
                detail.setParameter(param);
            }
            detail.setParameterName(r.getParameterName());
            detail.setObservedValue(r.getObservedValue());
            detail.setNumericValue(r.getNumericValue());
            detail.setResult(r.getResult() != null ? r.getResult() : ParameterResult.NOT_TESTED);
            detail.setRemarks(r.getRemarks());
            return detail;
        }).collect(Collectors.toList());
    }

    public TestBatchDto toDto(TestBatch b) {
        TestBatchDto dto = new TestBatchDto();
        dto.setId(b.getId());
        dto.setTenantId(b.getTenantId());
        dto.setBatchNumber(b.getBatchNumber());
        if (b.getDefinition() != null) {
            dto.setDefinitionId(b.getDefinition().getId());
            dto.setDefinitionName(b.getDefinition().getName());
        }
        if (b.getSample() != null) {
            dto.setSampleId(b.getSample().getId());
            dto.setSampleNumber(b.getSample().getSampleNumber());
        }
        dto.setTestDate(b.getTestDate());
        dto.setTestedBy(b.getTestedBy());
        dto.setLabLocation(b.getLabLocation());
        dto.setStatus(b.getStatus());
        dto.setOverallResult(b.getOverallResult());
        dto.setNotes(b.getNotes());
        dto.setCreatedAt(b.getCreatedAt());
        if (b.getResults() != null) {
            dto.setResults(b.getResults().stream().map(r -> {
                TestBatchDto.ResultDetailDto rdto = new TestBatchDto.ResultDetailDto();
                rdto.setId(r.getId());
                if (r.getParameter() != null) rdto.setParameterId(r.getParameter().getId());
                rdto.setParameterName(r.getParameterName());
                rdto.setObservedValue(r.getObservedValue());
                rdto.setNumericValue(r.getNumericValue());
                rdto.setResult(r.getResult());
                rdto.setRemarks(r.getRemarks());
                return rdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private TestBatch findOrThrow(UUID id) {
        return testBatchRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Test batch not found: " + id));
    }

    private String generateBatchNumber(UUID tenantId) {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = testBatchRepository.countByTenantIdAndDeletedAtIsNull(tenantId) + 1;
        return String.format("TB-%s-%03d", year, count);
    }
}

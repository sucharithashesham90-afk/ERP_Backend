package com.erp.platform.modules.quality.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.quality.dto.CreateTestDefinitionRequest;
import com.erp.platform.modules.quality.dto.TestDefinitionDto;
import com.erp.platform.modules.quality.entity.TestDefinition;
import com.erp.platform.modules.quality.entity.TestParameter;
import com.erp.platform.modules.quality.repository.TestDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TestDefinitionService {

    private final TestDefinitionRepository testDefinitionRepository;
    private final TenantContext tenantContext;

    public PageResponse<TestDefinitionDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(testDefinitionRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                .map(this::toDto));
    }

    public TestDefinitionDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public TestDefinitionDto create(CreateTestDefinitionRequest request) {
        UUID tenantId = tenantContext.current();
        if (testDefinitionRepository.existsByTenantIdAndCodeAndDeletedAtIsNull(tenantId, request.getCode())) {
            throw AppException.conflict("Test definition with code already exists: " + request.getCode());
        }
        TestDefinition definition = new TestDefinition();
        definition.setTenantId(tenantId);
        definition.setName(request.getName());
        definition.setCode(request.getCode());
        definition.setTestType(request.getTestType());
        definition.setApplicableTo(request.getApplicableTo());
        definition.setDescription(request.getDescription());
        definition.setStandardReference(request.getStandardReference());
        definition.setShortName(request.getShortName());
        definition.setNoOfReplications(request.getNoOfReplications());
        definition.setTestLocationIds(request.getTestLocationIds());
        definition.setActive(request.isActive());

        List<TestParameter> parameters = buildParameters(definition, request.getParameters(), tenantId);
        definition.setParameters(parameters);

        TestDefinition saved = testDefinitionRepository.save(definition);
        log.info("TestDefinition created: id={}, code={}", saved.getId(), saved.getCode());
        return toDto(saved);
    }

    @Transactional
    public TestDefinitionDto update(UUID id, CreateTestDefinitionRequest request) {
        UUID tenantId = tenantContext.current();
        TestDefinition definition = findOrThrow(id);
        definition.setName(request.getName());
        definition.setTestType(request.getTestType());
        definition.setApplicableTo(request.getApplicableTo());
        definition.setDescription(request.getDescription());
        definition.setStandardReference(request.getStandardReference());
        definition.setShortName(request.getShortName());
        definition.setNoOfReplications(request.getNoOfReplications());
        definition.setTestLocationIds(request.getTestLocationIds());
        definition.setActive(request.isActive());

        definition.getParameters().clear();
        List<TestParameter> parameters = buildParameters(definition, request.getParameters(), tenantId);
        definition.getParameters().addAll(parameters);

        return toDto(testDefinitionRepository.save(definition));
    }

    @Transactional
    public void delete(UUID id) {
        TestDefinition definition = findOrThrow(id);
        definition.setDeletedAt(LocalDateTime.now());
        testDefinitionRepository.save(definition);
        log.info("TestDefinition soft-deleted: id={}", id);
    }

    private List<TestParameter> buildParameters(TestDefinition definition,
            List<CreateTestDefinitionRequest.ParameterRequest> requests, UUID tenantId) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            TestParameter param = new TestParameter();
            param.setTenantId(tenantId);
            param.setDefinition(definition);
            param.setParameterName(r.getParameterName());
            param.setShortName(r.getShortName());
            param.setUnit(r.getUnit());
            param.setMinValue(r.getMinValue());
            param.setMaxValue(r.getMaxValue());
            param.setTargetValue(r.getTargetValue());
            param.setTestMethod(r.getTestMethod());
            param.setMandatory(r.isMandatory());
            param.setDisplayOrder(r.getDisplayOrder());
            param.setResultType(r.getResultType() != null ? r.getResultType() : "%");
            param.setDisplay(r.isDisplay());
            param.setResultAffects(r.isResultAffects());
            param.setKeyParam(r.isKeyParam());
            return param;
        }).collect(Collectors.toList());
    }

    public TestDefinitionDto toDto(TestDefinition d) {
        TestDefinitionDto dto = new TestDefinitionDto();
        dto.setId(d.getId());
        dto.setTenantId(d.getTenantId());
        dto.setName(d.getName());
        dto.setCode(d.getCode());
        dto.setTestType(d.getTestType());
        dto.setApplicableTo(d.getApplicableTo());
        dto.setDescription(d.getDescription());
        dto.setStandardReference(d.getStandardReference());
        dto.setShortName(d.getShortName());
        dto.setNoOfReplications(d.getNoOfReplications());
        dto.setTestLocationIds(d.getTestLocationIds());
        dto.setActive(d.isActive());
        dto.setCreatedAt(d.getCreatedAt());
        if (d.getParameters() != null) {
            dto.setParameters(d.getParameters().stream().map(p -> {
                TestDefinitionDto.ParameterDto pdto = new TestDefinitionDto.ParameterDto();
                pdto.setId(p.getId());
                pdto.setParameterName(p.getParameterName());
                pdto.setUnit(p.getUnit());
                pdto.setMinValue(p.getMinValue());
                pdto.setMaxValue(p.getMaxValue());
                pdto.setTargetValue(p.getTargetValue());
                pdto.setTestMethod(p.getTestMethod());
                pdto.setMandatory(p.isMandatory());
                pdto.setDisplayOrder(p.getDisplayOrder());
                pdto.setShortName(p.getShortName());
                pdto.setResultType(p.getResultType());
                pdto.setDisplay(p.isDisplay());
                pdto.setResultAffects(p.isResultAffects());
                pdto.setKeyParam(p.isKeyParam());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private TestDefinition findOrThrow(UUID id) {
        return testDefinitionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Test definition not found: " + id));
    }
}

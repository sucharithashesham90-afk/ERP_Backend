package com.erp.platform.modules.manufacturing.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.OperationType;
import com.erp.platform.modules.manufacturing.entity.ProcessStep;
import com.erp.platform.modules.manufacturing.entity.ProcessTemplate;
import com.erp.platform.modules.manufacturing.entity.ProcessTemplateStep;
import com.erp.platform.modules.manufacturing.repository.OperationTypeRepository;
import com.erp.platform.modules.manufacturing.repository.ProcessStepRepository;
import com.erp.platform.modules.manufacturing.repository.ProcessTemplateRepository;
import com.erp.platform.modules.manufacturing.repository.ProcessTemplateStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProcessStepService {

    private final OperationTypeRepository operationTypeRepo;
    private final ProcessStepRepository processStepRepo;
    private final ProcessTemplateRepository templateRepo;
    private final ProcessTemplateStepRepository templateStepRepo;
    private final TenantContext tenantContext;

    // ---- Operation Types ----

    public PageResponse<OperationType> listOperationTypes(Pageable pageable) {
        return PageResponse.of(operationTypeRepo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    @Transactional
    public OperationType createOperationType(OperationType req) {
        req.setTenantId(tenantContext.current());
        return operationTypeRepo.save(req);
    }

    @Transactional
    public OperationType updateOperationType(UUID id, OperationType req) {
        OperationType e = operationTypeRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Operation type not found: " + id));
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setDescription(req.getDescription());
        e.setCategory(req.getCategory());
        e.setActive(req.isActive());
        return operationTypeRepo.save(e);
    }

    @Transactional
    public void deleteOperationType(UUID id) {
        OperationType e = operationTypeRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Operation type not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        operationTypeRepo.save(e);
    }

    // ---- Process Steps ----

    public PageResponse<ProcessStep> listSteps(Pageable pageable) {
        return PageResponse.of(processStepRepo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    public List<ProcessStep> getAllActiveSteps() {
        return processStepRepo.findByTenantIdAndActiveAndDeletedAtIsNull(tenantContext.current(), true);
    }

    @Transactional
    public ProcessStep createStep(ProcessStep req) {
        req.setTenantId(tenantContext.current());
        return processStepRepo.save(req);
    }

    @Transactional
    public ProcessStep updateStep(UUID id, ProcessStep req) {
        ProcessStep e = processStepRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Process step not found: " + id));
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setOperationType(req.getOperationType());
        e.setDescription(req.getDescription());
        e.setStandardDurationHours(req.getStandardDurationHours());
        e.setSetupTimeHours(req.getSetupTimeHours());
        e.setEquipmentRequired(req.getEquipmentRequired());
        e.setSkillRequired(req.getSkillRequired());
        e.setQualityCheckRequired(req.isQualityCheckRequired());
        e.setInstructions(req.getInstructions());
        e.setActive(req.isActive());
        // The screen sends these on every save; copying them field-by-field meant an edit silently
        // reverted whatever the form had just set. Crops was the reported symptom, but output state,
        // input states and the four flags were being dropped the same way.
        e.setOutputState(req.getOutputState());
        e.setInputStates(req.getInputStates());
        e.setCrops(req.getCrops());
        e.setHasProcessLoss(req.isHasProcessLoss());
        e.setHasWaste(req.isHasWaste());
        e.setHasByproduct(req.isHasByproduct());
        e.setAllowMultipleInputs(req.isAllowMultipleInputs());
        return processStepRepo.save(e);
    }

    @Transactional
    public void deleteStep(UUID id) {
        ProcessStep e = processStepRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Process step not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        processStepRepo.save(e);
    }

    // ---- Process Templates ----

    public PageResponse<ProcessTemplate> listTemplates(Pageable pageable) {
        return PageResponse.of(templateRepo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    @Transactional
    public ProcessTemplate createTemplate(ProcessTemplate req) {
        req.setTenantId(tenantContext.current());
        return templateRepo.save(req);
    }

    @Transactional
    public ProcessTemplate updateTemplate(UUID id, ProcessTemplate req) {
        ProcessTemplate e = templateRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Process template not found: " + id));
        e.setName(req.getName());
        e.setCode(req.getCode());
        e.setDescription(req.getDescription());
        e.setProductCategory(req.getProductCategory());
        e.setActive(req.isActive());
        return templateRepo.save(e);
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        ProcessTemplate e = templateRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Process template not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        templateRepo.save(e);
    }

    // ---- Template Steps ----

    public List<ProcessTemplateStep> getTemplateSteps(UUID templateId) {
        return templateStepRepo.findByTemplateIdOrderBySequenceNumber(templateId);
    }

    @Transactional
    public ProcessTemplateStep addStepToTemplate(UUID templateId, ProcessTemplateStep req) {
        ProcessTemplate template = templateRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), templateId)
                .orElseThrow(() -> AppException.notFound("Process template not found: " + templateId));
        req.setTemplate(template);
        req.setTenantId(tenantContext.current());
        if (req.getSequenceNumber() <= 0) {
            long count = templateStepRepo.countByTemplateId(templateId);
            req.setSequenceNumber((int) count + 1);
        }
        return templateStepRepo.save(req);
    }

    @Transactional
    public void removeStepFromTemplate(UUID stepId) {
        ProcessTemplateStep e = templateStepRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), stepId)
                .orElseThrow(() -> AppException.notFound("Template step not found: " + stepId));
        e.setDeletedAt(LocalDateTime.now());
        templateStepRepo.save(e);
    }

    @Transactional
    public List<ProcessTemplateStep> reorderSteps(UUID templateId, List<UUID> stepIds) {
        List<ProcessTemplateStep> results = new ArrayList<>();
        UUID tenantId = tenantContext.current();
        for (int i = 0; i < stepIds.size(); i++) {
            UUID stepId = stepIds.get(i);
            templateStepRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, stepId).ifPresent(s -> {
                s.setSequenceNumber(stepIds.indexOf(stepId) + 1);
                results.add(templateStepRepo.save(s));
            });
        }
        return results;
    }
}

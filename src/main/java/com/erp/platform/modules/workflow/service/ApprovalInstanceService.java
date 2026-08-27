package com.erp.platform.modules.workflow.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.workflow.dto.ApprovalInstanceDto;
import com.erp.platform.modules.workflow.entity.ApprovalInstance;
import com.erp.platform.modules.workflow.entity.ApprovalInstance.ApprovalStatus;
import com.erp.platform.modules.workflow.entity.ApprovalRule;
import com.erp.platform.modules.workflow.entity.ApprovalStep;
import com.erp.platform.modules.workflow.entity.ApprovalStep.StepStatus;
import com.erp.platform.modules.workflow.repository.ApprovalInstanceRepository;
import com.erp.platform.modules.workflow.repository.ApprovalRuleRepository;
import com.erp.platform.modules.workflow.repository.ApprovalStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApprovalInstanceService {

    private final ApprovalInstanceRepository approvalInstanceRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final ApprovalRuleRepository approvalRuleRepository;
    private final TenantContext tenantContext;

    public PageResponse<ApprovalInstanceDto> list(String documentType, ApprovalStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<ApprovalInstance> page;
        if (documentType != null && status != null) {
            page = approvalInstanceRepository.findByTenantIdAndDocumentTypeAndDeletedAtIsNull(tenantId, documentType, pageable);
        } else if (documentType != null) {
            page = approvalInstanceRepository.findByTenantIdAndDocumentTypeAndDeletedAtIsNull(tenantId, documentType, pageable);
        } else if (status != null) {
            page = approvalInstanceRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = approvalInstanceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.of(page.map(this::toDto));
    }

    public ApprovalInstanceDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    public ApprovalInstanceDto getByDocument(String documentType, UUID documentId) {
        UUID tenantId = tenantContext.current();
        ApprovalInstance instance = approvalInstanceRepository
                .findByTenantIdAndDocumentIdAndDeletedAtIsNull(tenantId, documentId)
                .orElseThrow(() -> AppException.notFound("Approval instance not found for document: " + documentId));
        return toDto(instance);
    }

    @Transactional
    public ApprovalInstanceDto submit(String documentType, UUID documentId, String documentNumber,
                                      String submittedBy, int maxLevel) {
        UUID tenantId = tenantContext.current();

        ApprovalInstance instance = new ApprovalInstance();
        instance.setTenantId(tenantId);
        instance.setDocumentType(documentType);
        instance.setDocumentId(documentId);
        instance.setDocumentNumber(documentNumber);
        instance.setCurrentLevel(1);
        instance.setMaxLevel(maxLevel);
        instance.setStatus(ApprovalStatus.IN_PROGRESS);
        instance.setSubmittedBy(submittedBy);
        instance.setSubmittedAt(LocalDateTime.now());
        ApprovalInstance saved = approvalInstanceRepository.save(instance);

        // Create first step
        ApprovalStep step = new ApprovalStep();
        step.setTenantId(tenantId);
        step.setInstance(saved);
        step.setLevel(1);
        step.setStatus(StepStatus.PENDING);

        // Look up matching rule for SLA
        List<ApprovalRule> rules = approvalRuleRepository
                .findByTenantIdAndDocumentTypeAndActiveAndDeletedAtIsNull(tenantId, documentType, true);

        Optional<ApprovalRule> firstLevelRule = rules.stream()
                .filter(r -> r.getApprovalLevel() == 1)
                .findFirst();

        if (firstLevelRule.isPresent()) {
            ApprovalRule rule = firstLevelRule.get();
            step.setApproverType(rule.getApproverType() != null ? rule.getApproverType().name() : null);
            step.setApproverValue(rule.getApproverValue());
            step.setDueDate(LocalDateTime.now().plusDays(rule.getSlaDays()));
        } else {
            step.setDueDate(LocalDateTime.now().plusDays(3));
        }

        approvalStepRepository.save(step);
        saved.getSteps().add(step);

        log.info("ApprovalInstance submitted: id={}, document={}/{}", saved.getId(), documentType, documentId);
        return toDto(saved);
    }

    @Transactional
    public ApprovalInstanceDto approve(UUID instanceId, String assignedTo, String comments) {
        UUID tenantId = tenantContext.current();
        ApprovalInstance instance = findOrThrow(instanceId);

        List<ApprovalStep> steps = approvalStepRepository
                .findByTenantIdAndInstance_IdAndDeletedAtIsNull(tenantId, instanceId);

        ApprovalStep currentStep = steps.stream()
                .filter(s -> s.getStatus() == StepStatus.PENDING && s.getLevel() == instance.getCurrentLevel())
                .findFirst()
                .orElseThrow(() -> AppException.badRequest("No pending step found at current level"));

        currentStep.setStatus(StepStatus.APPROVED);
        currentStep.setAssignedTo(assignedTo);
        currentStep.setComments(comments);
        currentStep.setActionDate(LocalDateTime.now());
        approvalStepRepository.save(currentStep);

        if (instance.getCurrentLevel() < instance.getMaxLevel()) {
            // Create next level step
            int nextLevel = instance.getCurrentLevel() + 1;
            instance.setCurrentLevel(nextLevel);

            ApprovalStep nextStep = new ApprovalStep();
            nextStep.setTenantId(tenantId);
            nextStep.setInstance(instance);
            nextStep.setLevel(nextLevel);
            nextStep.setStatus(StepStatus.PENDING);

            List<ApprovalRule> rules = approvalRuleRepository
                    .findByTenantIdAndDocumentTypeAndActiveAndDeletedAtIsNull(tenantId, instance.getDocumentType(), true);
            rules.stream()
                    .filter(r -> r.getApprovalLevel() == nextLevel)
                    .findFirst()
                    .ifPresent(rule -> {
                        nextStep.setApproverType(rule.getApproverType() != null ? rule.getApproverType().name() : null);
                        nextStep.setApproverValue(rule.getApproverValue());
                        nextStep.setDueDate(LocalDateTime.now().plusDays(rule.getSlaDays()));
                    });

            if (nextStep.getDueDate() == null) {
                nextStep.setDueDate(LocalDateTime.now().plusDays(3));
            }

            approvalStepRepository.save(nextStep);
        } else {
            instance.setStatus(ApprovalStatus.APPROVED);
            instance.setCompletedAt(LocalDateTime.now());
        }

        approvalInstanceRepository.save(instance);
        log.info("ApprovalInstance approved at level={}: id={}", instance.getCurrentLevel(), instanceId);
        return toDto(instance);
    }

    @Transactional
    public ApprovalInstanceDto reject(UUID instanceId, String assignedTo, String comments) {
        UUID tenantId = tenantContext.current();
        ApprovalInstance instance = findOrThrow(instanceId);

        List<ApprovalStep> steps = approvalStepRepository
                .findByTenantIdAndInstance_IdAndDeletedAtIsNull(tenantId, instanceId);

        ApprovalStep currentStep = steps.stream()
                .filter(s -> s.getStatus() == StepStatus.PENDING && s.getLevel() == instance.getCurrentLevel())
                .findFirst()
                .orElseThrow(() -> AppException.badRequest("No pending step found at current level"));

        currentStep.setStatus(StepStatus.REJECTED);
        currentStep.setAssignedTo(assignedTo);
        currentStep.setComments(comments);
        currentStep.setActionDate(LocalDateTime.now());
        approvalStepRepository.save(currentStep);

        instance.setStatus(ApprovalStatus.REJECTED);
        instance.setCompletedAt(LocalDateTime.now());
        approvalInstanceRepository.save(instance);

        log.info("ApprovalInstance rejected: id={}", instanceId);
        return toDto(instance);
    }

    @Transactional
    public ApprovalInstanceDto cancel(UUID instanceId) {
        ApprovalInstance instance = findOrThrow(instanceId);
        if (instance.getStatus() == ApprovalStatus.APPROVED || instance.getStatus() == ApprovalStatus.REJECTED) {
            throw AppException.badRequest("Cannot cancel a completed approval instance");
        }
        instance.setStatus(ApprovalStatus.CANCELLED);
        instance.setCompletedAt(LocalDateTime.now());
        approvalInstanceRepository.save(instance);
        log.info("ApprovalInstance cancelled: id={}", instanceId);
        return toDto(instance);
    }

    private ApprovalInstance findOrThrow(UUID id) {
        return approvalInstanceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Approval instance not found: " + id));
    }

    private ApprovalInstanceDto toDto(ApprovalInstance i) {
        ApprovalInstanceDto dto = new ApprovalInstanceDto();
        dto.setId(i.getId());
        dto.setDocumentType(i.getDocumentType());
        dto.setDocumentId(i.getDocumentId());
        dto.setDocumentNumber(i.getDocumentNumber());
        dto.setCurrentLevel(i.getCurrentLevel());
        dto.setMaxLevel(i.getMaxLevel());
        dto.setStatus(i.getStatus());
        dto.setSubmittedBy(i.getSubmittedBy());
        dto.setSubmittedAt(i.getSubmittedAt());
        dto.setCompletedAt(i.getCompletedAt());
        dto.setNotes(i.getNotes());
        dto.setCreatedAt(i.getCreatedAt());
        if (i.getSteps() != null) {
            dto.setSteps(i.getSteps().stream().map(s -> {
                ApprovalInstanceDto.StepDto sd = new ApprovalInstanceDto.StepDto();
                sd.setId(s.getId());
                sd.setLevel(s.getLevel());
                sd.setApproverType(s.getApproverType());
                sd.setApproverValue(s.getApproverValue());
                sd.setAssignedTo(s.getAssignedTo());
                sd.setStatus(s.getStatus());
                sd.setActionDate(s.getActionDate());
                sd.setDueDate(s.getDueDate());
                sd.setComments(s.getComments());
                sd.setEscalatedAt(s.getEscalatedAt());
                return sd;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}

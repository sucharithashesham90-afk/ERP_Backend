package com.erp.platform.modules.workflow.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.workflow.entity.WorkflowApproval;
import com.erp.platform.modules.workflow.entity.WorkflowDefinition;
import com.erp.platform.modules.workflow.entity.WorkflowInstance;
import com.erp.platform.modules.workflow.entity.WorkflowInstance.WorkflowStatus;
import com.erp.platform.modules.workflow.entity.WorkflowStep;
import com.erp.platform.modules.workflow.repository.WorkflowDefinitionRepository;
import com.erp.platform.modules.workflow.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkflowService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final WorkflowInstanceRepository instanceRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TenantContext tenantContext;

    public PageResponse<WorkflowDefinition> listDefinitions(String module, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (module != null) {
            List<WorkflowDefinition> defs = definitionRepository
                    .findByTenantIdAndModuleAndActiveTrueAndDeletedAtIsNull(tenantId, module);
            // Wrap list in page
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), defs.size());
            List<WorkflowDefinition> page = start > defs.size() ? List.of() : defs.subList(start, end);
            return PageResponse.of(new org.springframework.data.domain.PageImpl<>(page, pageable, defs.size()));
        }
        return PageResponse.of(definitionRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    @Transactional
    public WorkflowDefinition createDefinition(WorkflowDefinition def) {
        UUID tenantId = tenantContext.current();
        def.setTenantId(tenantId);
        if (def.getSteps() != null) def.getSteps().forEach(s -> s.setWorkflow(def));
        WorkflowDefinition saved = definitionRepository.save(def);
        log.info("WorkflowDefinition created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    public PageResponse<WorkflowInstance> listInstances(WorkflowStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? instanceRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : instanceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page);
    }

    @Transactional
    public WorkflowInstance initiateWorkflow(String module, UUID referenceId, String referenceNumber,
                                              BigDecimal amount, UUID initiatedByUserId) {
        UUID tenantId = tenantContext.current();
        List<WorkflowDefinition> defs = definitionRepository
                .findByTenantIdAndModuleAndActiveTrueAndDeletedAtIsNull(tenantId, module);
        WorkflowDefinition applicable = defs.stream()
                .filter(d -> {
                    boolean minOk = d.getMinAmount() == null || amount == null || amount.compareTo(d.getMinAmount()) >= 0;
                    boolean maxOk = d.getMaxAmount() == null || amount == null || amount.compareTo(d.getMaxAmount()) <= 0;
                    return minOk && maxOk;
                })
                .findFirst().orElse(null);

        if (applicable == null) return null;

        WorkflowInstance instance = new WorkflowInstance();
        instance.setTenantId(tenantId);
        instance.setWorkflow(applicable);
        instance.setModule(module);
        instance.setReferenceId(referenceId);
        instance.setReferenceNumber(referenceNumber);
        instance.setAmount(amount);
        instance.setStatus(WorkflowStatus.PENDING);
        instance.setCurrentStep(1);
        instance.setInitiatedBy(initiatedByUserId);

        List<WorkflowApproval> approvals = new ArrayList<>();
        for (WorkflowStep step : applicable.getSteps()) {
            WorkflowApproval approval = new WorkflowApproval();
            approval.setInstance(instance);
            approval.setStepOrder(step.getStepOrder());
            approval.setStepName(step.getStepName());
            approval.setStatus(step.getStepOrder() == 1 ? "PENDING" : "WAITING");
            approvals.add(approval);
        }
        instance.setApprovals(approvals);
        return instanceRepository.save(instance);
    }

    @Transactional
    public WorkflowInstance processApproval(UUID instanceId, UUID approverId, String action, String comments) {
        WorkflowInstance instance = instanceRepository.findByTenantIdAndIdAndDeletedAtIsNull(
                tenantContext.current(), instanceId)
                .orElseThrow(() -> AppException.notFound("Workflow instance not found: " + instanceId));

        if (instance.getStatus() != WorkflowStatus.PENDING) {
            throw AppException.badRequest("Workflow is not in pending state");
        }

        WorkflowApproval currentApproval = instance.getApprovals().stream()
                .filter(a -> a.getStepOrder() == instance.getCurrentStep() && "PENDING".equals(a.getStatus()))
                .findFirst()
                .orElseThrow(() -> AppException.badRequest("No pending approval found for current step"));

        currentApproval.setApproverId(approverId);
        currentApproval.setAction(action);
        currentApproval.setComments(comments);
        currentApproval.setActionAt(LocalDateTime.now());
        currentApproval.setStatus("COMPLETED");

        if ("REJECTED".equals(action)) {
            instance.setStatus(WorkflowStatus.REJECTED);
            instance.setCompletedAt(LocalDateTime.now());
        } else if ("APPROVED".equals(action)) {
            int nextStep = instance.getCurrentStep() + 1;
            boolean hasNextStep = instance.getApprovals().stream()
                    .anyMatch(a -> a.getStepOrder() == nextStep);
            if (hasNextStep) {
                instance.setCurrentStep(nextStep);
                instance.getApprovals().stream()
                        .filter(a -> a.getStepOrder() == nextStep)
                        .findFirst()
                        .ifPresent(a -> a.setStatus("PENDING"));
            } else {
                instance.setStatus(WorkflowStatus.APPROVED);
                instance.setCompletedAt(LocalDateTime.now());
            }
        }

        WorkflowInstance saved = instanceRepository.save(instance);

        // Notify other modules when workflow reaches a terminal state
        if (saved.getStatus() == WorkflowStatus.APPROVED || saved.getStatus() == WorkflowStatus.REJECTED) {
            log.info("Workflow {} completed: module={}, ref={}, outcome={}",
                    instanceId, saved.getModule(), saved.getReferenceNumber(), saved.getStatus());
            eventPublisher.publishEvent(new WorkflowCompletedEvent(this, saved));
        }

        return saved;
    }
}

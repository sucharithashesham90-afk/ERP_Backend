package com.erp.platform.modules.workflow.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.workflow.dto.ApprovalRuleDto;
import com.erp.platform.modules.workflow.dto.CreateApprovalRuleRequest;
import com.erp.platform.modules.workflow.entity.ApprovalRule;
import com.erp.platform.modules.workflow.repository.ApprovalRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApprovalRuleService {

    private final ApprovalRuleRepository approvalRuleRepository;
    private final TenantContext tenantContext;

    public PageResponse<ApprovalRuleDto> list(String documentType, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<ApprovalRule> page = documentType != null
                ? approvalRuleRepository.findByTenantIdAndDocumentTypeAndDeletedAtIsNull(tenantId, documentType, pageable)
                : approvalRuleRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    @Transactional
    public ApprovalRuleDto create(CreateApprovalRuleRequest request) {
        UUID tenantId = tenantContext.current();
        ApprovalRule rule = new ApprovalRule();
        rule.setTenantId(tenantId);
        mapRequestToEntity(request, rule);
        ApprovalRule saved = approvalRuleRepository.save(rule);
        log.info("ApprovalRule created: id={}, name={}", saved.getId(), saved.getRuleName());
        return toDto(saved);
    }

    @Transactional
    public ApprovalRuleDto update(UUID id, CreateApprovalRuleRequest request) {
        ApprovalRule rule = findOrThrow(id);
        mapRequestToEntity(request, rule);
        return toDto(approvalRuleRepository.save(rule));
    }

    @Transactional
    public void delete(UUID id) {
        ApprovalRule rule = findOrThrow(id);
        rule.setDeletedAt(LocalDateTime.now());
        approvalRuleRepository.save(rule);
        log.info("ApprovalRule soft-deleted: id={}", id);
    }

    private void mapRequestToEntity(CreateApprovalRuleRequest request, ApprovalRule rule) {
        rule.setWorkflowDefinitionId(request.getWorkflowDefinitionId());
        rule.setRuleName(request.getRuleName());
        rule.setDocumentType(request.getDocumentType());
        rule.setConditionField(request.getConditionField());
        rule.setConditionOperator(request.getConditionOperator());
        rule.setConditionValue(request.getConditionValue());
        rule.setConditionValue2(request.getConditionValue2());
        rule.setApproverType(request.getApproverType());
        rule.setApproverValue(request.getApproverValue());
        rule.setApprovalLevel(request.getApprovalLevel());
        rule.setSlaDays(request.getSlaDays() > 0 ? request.getSlaDays() : 3);
        rule.setEscalateTo(request.getEscalateTo());
        rule.setActive(request.isActive());
    }

    private ApprovalRule findOrThrow(UUID id) {
        return approvalRuleRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Approval rule not found: " + id));
    }

    private ApprovalRuleDto toDto(ApprovalRule r) {
        ApprovalRuleDto dto = new ApprovalRuleDto();
        dto.setId(r.getId());
        dto.setWorkflowDefinitionId(r.getWorkflowDefinitionId());
        dto.setRuleName(r.getRuleName());
        dto.setDocumentType(r.getDocumentType());
        dto.setConditionField(r.getConditionField());
        dto.setConditionOperator(r.getConditionOperator());
        dto.setConditionValue(r.getConditionValue());
        dto.setConditionValue2(r.getConditionValue2());
        dto.setApproverType(r.getApproverType());
        dto.setApproverValue(r.getApproverValue());
        dto.setApprovalLevel(r.getApprovalLevel());
        dto.setSlaDays(r.getSlaDays());
        dto.setEscalateTo(r.getEscalateTo());
        dto.setActive(r.isActive());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}

package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.dto.CreatePurchaseRequisitionRequest;
import com.erp.platform.modules.purchase.dto.PurchaseRequisitionDto;
import com.erp.platform.modules.purchase.entity.PurchaseRequisition;
import com.erp.platform.modules.purchase.entity.PurchaseRequisition.Priority;
import com.erp.platform.modules.purchase.entity.PurchaseRequisition.ReqStatus;
import com.erp.platform.modules.purchase.entity.PurchaseRequisitionItem;
import com.erp.platform.modules.purchase.repository.PurchaseRequisitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
public class PurchaseRequisitionService {

    private final PurchaseRequisitionRepository requisitionRepository;
    private final TenantContext tenantContext;

    public PageResponse<PurchaseRequisitionDto> list(ReqStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? requisitionRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : requisitionRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public PurchaseRequisitionDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PurchaseRequisitionDto create(CreatePurchaseRequisitionRequest request) {
        UUID tenantId = tenantContext.current();
        PurchaseRequisition req = new PurchaseRequisition();
        req.setTenantId(tenantId);
        req.setRequisitionNumber(generateRequisitionNumber(tenantId));
        req.setRequestedBy(request.getRequestedBy());
        req.setDepartmentId(request.getDepartmentId());
        req.setDepartmentName(request.getDepartmentName());
        req.setRequiredByDate(request.getRequiredByDate());
        req.setStatus(ReqStatus.DRAFT);
        req.setPriority(request.getPriority() != null ? request.getPriority() : Priority.NORMAL);
        req.setNotes(request.getNotes());

        List<PurchaseRequisitionItem> items = buildItems(req, request.getItems(), tenantId);
        req.setItems(items);
        req.setTotalEstimatedValue(items.stream()
                .map(i -> i.getEstimatedTotalPrice() != null ? i.getEstimatedTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        PurchaseRequisition saved = requisitionRepository.save(req);
        log.info("PurchaseRequisition created: id={}, number={}", saved.getId(), saved.getRequisitionNumber());
        return toDto(saved);
    }

    @Transactional
    public PurchaseRequisitionDto update(UUID id, CreatePurchaseRequisitionRequest request) {
        UUID tenantId = tenantContext.current();
        PurchaseRequisition req = findOrThrow(id);
        if (req.getStatus() != ReqStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT requisitions can be updated");
        }
        req.setRequestedBy(request.getRequestedBy());
        req.setDepartmentId(request.getDepartmentId());
        req.setDepartmentName(request.getDepartmentName());
        req.setRequiredByDate(request.getRequiredByDate());
        req.setPriority(request.getPriority() != null ? request.getPriority() : req.getPriority());
        req.setNotes(request.getNotes());

        req.getItems().clear();
        List<PurchaseRequisitionItem> items = buildItems(req, request.getItems(), tenantId);
        req.getItems().addAll(items);
        req.setTotalEstimatedValue(items.stream()
                .map(i -> i.getEstimatedTotalPrice() != null ? i.getEstimatedTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return toDto(requisitionRepository.save(req));
    }

    @Transactional
    public PurchaseRequisitionDto approve(UUID id, String approver) {
        PurchaseRequisition req = findOrThrow(id);
        if (req.getStatus() != ReqStatus.SUBMITTED) {
            throw AppException.badRequest("Only SUBMITTED requisitions can be approved");
        }
        req.setStatus(ReqStatus.APPROVED);
        req.setApprovedBy(approver);
        req.setApprovalDate(LocalDate.now());
        return toDto(requisitionRepository.save(req));
    }

    @Transactional
    public PurchaseRequisitionDto reject(UUID id, String reason) {
        PurchaseRequisition req = findOrThrow(id);
        if (req.getStatus() != ReqStatus.SUBMITTED) {
            throw AppException.badRequest("Only SUBMITTED requisitions can be rejected");
        }
        req.setStatus(ReqStatus.REJECTED);
        req.setRejectionReason(reason);
        return toDto(requisitionRepository.save(req));
    }

    @Transactional
    public PurchaseRequisitionDto submit(UUID id) {
        PurchaseRequisition req = findOrThrow(id);
        if (req.getStatus() != ReqStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT requisitions can be submitted");
        }
        req.setStatus(ReqStatus.SUBMITTED);
        return toDto(requisitionRepository.save(req));
    }

    @Transactional
    public void delete(UUID id) {
        PurchaseRequisition req = findOrThrow(id);
        if (req.getStatus() != ReqStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT requisitions can be deleted");
        }
        req.setDeletedAt(LocalDateTime.now());
        requisitionRepository.save(req);
        log.info("PurchaseRequisition soft-deleted: id={}", id);
    }

    private List<PurchaseRequisitionItem> buildItems(PurchaseRequisition req,
            List<CreatePurchaseRequisitionRequest.ItemRequest> requests, UUID tenantId) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            PurchaseRequisitionItem item = new PurchaseRequisitionItem();
            item.setTenantId(tenantId);
            item.setRequisition(req);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setQuantity(r.getQuantity());
            item.setUnit(r.getUnit());
            item.setEstimatedUnitPrice(r.getEstimatedUnitPrice());
            BigDecimal total = r.getEstimatedUnitPrice() != null && r.getQuantity() != null
                    ? r.getEstimatedUnitPrice().multiply(r.getQuantity())
                    : BigDecimal.ZERO;
            item.setEstimatedTotalPrice(total);
            item.setPreferredVendorId(r.getPreferredVendorId());
            item.setPreferredVendorName(r.getPreferredVendorName());
            item.setSpecifications(r.getSpecifications());
            item.setNotes(r.getNotes());
            return item;
        }).collect(Collectors.toList());
    }

    public PurchaseRequisitionDto toDto(PurchaseRequisition r) {
        PurchaseRequisitionDto dto = new PurchaseRequisitionDto();
        dto.setId(r.getId());
        dto.setTenantId(r.getTenantId());
        dto.setRequisitionNumber(r.getRequisitionNumber());
        dto.setRequestedBy(r.getRequestedBy());
        dto.setDepartmentId(r.getDepartmentId());
        dto.setDepartmentName(r.getDepartmentName());
        dto.setRequiredByDate(r.getRequiredByDate());
        dto.setStatus(r.getStatus());
        dto.setPriority(r.getPriority());
        dto.setTotalEstimatedValue(r.getTotalEstimatedValue());
        dto.setApprovedBy(r.getApprovedBy());
        dto.setApprovalDate(r.getApprovalDate());
        dto.setRejectionReason(r.getRejectionReason());
        dto.setNotes(r.getNotes());
        dto.setCreatedAt(r.getCreatedAt());
        if (r.getItems() != null) {
            dto.setItems(r.getItems().stream().map(i -> {
                PurchaseRequisitionDto.ItemDto idto = new PurchaseRequisitionDto.ItemDto();
                idto.setId(i.getId());
                idto.setProductId(i.getProductId());
                idto.setProductName(i.getProductName());
                idto.setQuantity(i.getQuantity());
                idto.setUnit(i.getUnit());
                idto.setEstimatedUnitPrice(i.getEstimatedUnitPrice());
                idto.setEstimatedTotalPrice(i.getEstimatedTotalPrice());
                idto.setPreferredVendorId(i.getPreferredVendorId());
                idto.setPreferredVendorName(i.getPreferredVendorName());
                idto.setSpecifications(i.getSpecifications());
                idto.setNotes(i.getNotes());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private PurchaseRequisition findOrThrow(UUID id) {
        return requisitionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Purchase requisition not found: " + id));
    }

    private String generateRequisitionNumber(UUID tenantId) {
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = requisitionRepository.countByTenantIdAndDeletedAtIsNull(tenantId) + 1;
        return String.format("REQ-%s-%03d", year, count);
    }
}

package com.erp.platform.modules.manufacturing.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.BatchEvent;
import com.erp.platform.modules.manufacturing.entity.BatchEvent.EventType;
import com.erp.platform.modules.manufacturing.entity.BatchHistory;
import com.erp.platform.modules.manufacturing.entity.BatchHistory.BatchStatus;
import com.erp.platform.modules.manufacturing.repository.BatchEventRepository;
import com.erp.platform.modules.manufacturing.repository.BatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BatchTrackingService {

    private final BatchHistoryRepository batchRepo;
    private final BatchEventRepository eventRepo;
    private final TenantContext tenantContext;

    public PageResponse<BatchHistory> list(UUID productId, BatchStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (productId != null) {
            return PageResponse.of(batchRepo.findByTenantIdAndProductIdAndDeletedAtIsNull(tenantId, productId, pageable));
        }
        if (status != null) {
            return PageResponse.of(batchRepo.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable));
        }
        return PageResponse.of(batchRepo.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public BatchHistory getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public BatchHistory create(BatchHistory req) {
        UUID tenantId = tenantContext.current();
        req.setTenantId(tenantId);
        req.setBatchNumber(generateBatchNumber(tenantId));
        req.setRemainingQuantity(req.getQuantity());
        req.setStatus(BatchStatus.CREATED);
        BatchHistory saved = batchRepo.save(req);

        BatchEvent event = new BatchEvent();
        event.setTenantId(tenantId);
        event.setBatch(saved);
        event.setEventType(EventType.CREATED);
        event.setEventDate(LocalDateTime.now());
        event.setDescription("Batch created with quantity: " + req.getQuantity());
        event.setQuantity(req.getQuantity());
        event.setPerformedBy("System");
        eventRepo.save(event);

        return saved;
    }

    @Transactional
    public BatchHistory update(UUID id, BatchHistory req) {
        BatchHistory e = findOrThrow(id);
        e.setProductionDate(req.getProductionDate());
        e.setExpiryDate(req.getExpiryDate());
        e.setUnit(req.getUnit());
        e.setWorkOrderId(req.getWorkOrderId());
        e.setNotes(req.getNotes());
        return batchRepo.save(e);
    }

    @Transactional
    public BatchEvent addEvent(UUID batchId, BatchEvent event) {
        BatchHistory batch = findOrThrow(batchId);
        event.setBatch(batch);
        event.setTenantId(tenantContext.current());
        if (event.getEventDate() == null) {
            event.setEventDate(LocalDateTime.now());
        }
        return eventRepo.save(event);
    }

    public List<BatchEvent> getEvents(UUID batchId) {
        findOrThrow(batchId);
        return eventRepo.findByBatchIdOrderByEventDateDesc(batchId);
    }

    @Transactional
    public BatchHistory updateStatus(UUID batchId, BatchStatus newStatus, String reason) {
        BatchHistory batch = findOrThrow(batchId);
        BatchStatus oldStatus = batch.getStatus();
        batch.setStatus(newStatus);
        batchRepo.save(batch);

        BatchEvent event = new BatchEvent();
        event.setTenantId(tenantContext.current());
        event.setBatch(batch);
        event.setEventType(EventType.STATUS_CHANGED);
        event.setEventDate(LocalDateTime.now());
        event.setDescription("Status changed from " + oldStatus + " to " + newStatus + (reason != null ? ": " + reason : ""));
        event.setPerformedBy("System");
        event.setResult(newStatus.name());
        eventRepo.save(event);

        return batch;
    }

    @Transactional
    public BatchHistory adjustQuantity(UUID batchId, BigDecimal qty, String reason) {
        BatchHistory batch = findOrThrow(batchId);
        BigDecimal newRemaining = batch.getRemainingQuantity().add(qty);
        if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
            throw AppException.businessRule("Adjustment would result in negative remaining quantity");
        }
        batch.setRemainingQuantity(newRemaining);
        batchRepo.save(batch);

        BatchEvent event = new BatchEvent();
        event.setTenantId(tenantContext.current());
        event.setBatch(batch);
        event.setEventType(EventType.ADJUSTED);
        event.setEventDate(LocalDateTime.now());
        event.setDescription("Quantity adjusted by " + qty + (reason != null ? ": " + reason : ""));
        event.setQuantity(qty);
        event.setPerformedBy("System");
        eventRepo.save(event);

        return batch;
    }

    private BatchHistory findOrThrow(UUID id) {
        return batchRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Batch not found: " + id));
    }

    private String generateBatchNumber(UUID tenantId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "BATCH-" + date + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

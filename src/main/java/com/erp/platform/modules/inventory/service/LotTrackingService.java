package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.LotHistoryEventDto;
import com.erp.platform.modules.inventory.dto.LotMovementDto;
import com.erp.platform.modules.inventory.dto.LotStockDto;
import com.erp.platform.modules.inventory.dto.LotTraceDto;
import com.erp.platform.modules.inventory.entity.LotMovement;
import com.erp.platform.modules.inventory.entity.LotMovement.LotMovType;
import com.erp.platform.modules.inventory.entity.LotStock;
import com.erp.platform.modules.inventory.entity.LotStock.LotStockStatus;
import com.erp.platform.modules.inventory.repository.LotMovementRepository;
import com.erp.platform.modules.inventory.repository.LotStockRepository;
import com.erp.platform.modules.manufacturing.entity.ProductionJob;
import com.erp.platform.modules.manufacturing.repository.ProductionJobRepository;
import com.erp.platform.modules.quality.entity.QualityInspection;
import com.erp.platform.modules.quality.repository.QualityInspectionRepository;
import com.erp.platform.modules.sales.repository.DeliveryNoteRepository;
import com.erp.platform.modules.sales.repository.SalesReturnRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LotTrackingService {

    private final LotStockRepository lotStockRepository;
    private final LotMovementRepository lotMovementRepository;
    private final ProductionJobRepository productionJobRepository;
    private final QualityInspectionRepository qualityInspectionRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final SalesReturnRepository salesReturnRepository;
    private final TenantContext tenantContext;

    public PageResponse<LotStockDto> listLots(UUID productId, UUID warehouseId,
            String productName, String lotNumber, LotStock.LotStockStatus statusFilter, Pageable pageable) {
        return listLots(productId, warehouseId, null, productName, lotNumber, statusFilter, pageable);
    }

    /**
     * Lots, optionally narrowed to one location.
     *
     * <p>The location filter takes a name, not an id, because that is what the screens hold: the
     * transfer forms carry a location as a name string and post it as a name. Matching on
     * warehouseId alone left them unable to filter at all, so a stock transfer offered every lot in
     * the company — including lots that were never at the location being transferred out of.
     *
     * <p>Both warehouseName and storageLocationName are checked, since which of the two carries the
     * name depends on how the lot was created.
     */
    public PageResponse<LotStockDto> listLots(UUID productId, UUID warehouseId, String location,
            String productName, String lotNumber, LotStock.LotStockStatus statusFilter, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Specification<LotStock> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (productId != null)
                predicates.add(cb.equal(root.get("productId"), productId));
            if (warehouseId != null)
                predicates.add(cb.equal(root.get("warehouseId"), warehouseId));
            if (location != null && !location.isBlank()) {
                String needle = location.trim().toLowerCase();
                predicates.add(cb.or(
                        cb.equal(cb.lower(root.get("warehouseName")), needle),
                        cb.equal(cb.lower(root.get("storageLocationName")), needle)));
            }
            if (productName != null && !productName.isBlank())
                predicates.add(cb.like(cb.lower(root.get("productName")),
                        "%" + productName.toLowerCase() + "%"));
            if (lotNumber != null && !lotNumber.isBlank())
                predicates.add(cb.like(cb.lower(root.get("lotNumber")),
                        "%" + lotNumber.toLowerCase() + "%"));
            if (statusFilter != null)
                predicates.add(cb.equal(root.get("status"), statusFilter));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(lotStockRepository.findAll(spec, pageable).map(this::toDto));
    }

    public LotTraceDto traceLot(String lotNumber) {
        UUID tenantId = tenantContext.current();
        List<LotStock> stocks = lotStockRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(tenantId, lotNumber);
        List<LotMovement> movements = lotMovementRepository
                .findByTenantIdAndLotNumberAndDeletedAtIsNullOrderByMovementDateAscCreatedAtAsc(tenantId, lotNumber);

        BigDecimal totalReceived = movements.stream()
                .filter(m -> m.getMovementType() == LotMovType.RECEIPT
                          || m.getMovementType() == LotMovType.TRANSFER_IN)
                .map(LotMovement::getQuantity)
                .filter(q -> q != null && q.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIssued = movements.stream()
                .filter(m -> m.getMovementType() == LotMovType.ISSUE
                          || m.getMovementType() == LotMovType.TRANSFER_OUT)
                .map(LotMovement::getQuantity)
                .filter(q -> q != null)
                .map(q -> q.compareTo(BigDecimal.ZERO) < 0 ? q.negate() : q)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentBalance = stocks.stream()
                .map(LotStock::getQuantityOnHand)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<LotMovementDto> movementDtos = movements.stream()
                .map(m -> toEnrichedMovementDto(m, tenantId))
                .collect(Collectors.toList());

        List<LotHistoryEventDto> events = buildEvents(tenantId, lotNumber, movements);

        LotTraceDto dto = new LotTraceDto();
        dto.setLotNumber(lotNumber);
        dto.setStocks(stocks.stream().map(this::toDto).collect(Collectors.toList()));
        dto.setMovements(movementDtos);
        dto.setEvents(events);
        dto.setTotalReceived(totalReceived);
        dto.setTotalIssued(totalIssued);
        dto.setCurrentBalance(currentBalance);
        return dto;
    }

    // ── Cross-module events ──────────────────────────────────────────────────

    private List<LotHistoryEventDto> buildEvents(UUID tenantId, String lotNumber, List<LotMovement> movements) {
        List<LotHistoryEventDto> events = new ArrayList<>();

        // 1. Production / Process Jobs where this lot is the OUTPUT
        for (ProductionJob job : productionJobRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(tenantId, lotNumber)) {
            events.add(toProductionJobEvent(job));
        }

        // 2. Quality Inspections for this lot
        for (QualityInspection qi : qualityInspectionRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(tenantId, lotNumber)) {
            events.add(toQualityEvent(qi));
        }

        // 3. Sales Dispatch events (from DELIVERY_NOTE movements)
        for (LotMovement m : movements) {
            if ("DELIVERY_NOTE".equals(m.getReferenceType()) && m.getReferenceId() != null) {
                events.add(toSalesDispatchEvent(m, tenantId));
            }
        }

        // 4. Sales Return events (from SALES_RETURN movements)
        for (LotMovement m : movements) {
            if ("SALES_RETURN".equals(m.getReferenceType()) && m.getReferenceId() != null) {
                events.add(toSalesReturnEvent(m, tenantId));
            }
        }

        events.sort(Comparator.comparing(
                e -> e.getEventDate() != null ? e.getEventDate() : "9999-12-31"));

        return events;
    }

    private LotHistoryEventDto toSalesDispatchEvent(LotMovement m, UUID tenantId) {
        LotHistoryEventDto e = new LotHistoryEventDto();
        e.setEventType("SALES_DISPATCH");
        e.setReferenceId(m.getReferenceId());
        e.setReferenceNumber(m.getReferenceNumber());
        e.setQuantity(m.getQuantity() != null ? m.getQuantity().abs() : null);

        deliveryNoteRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, m.getReferenceId())
                .ifPresent(dn -> {
                    e.setPartyName(dn.getCustomerName());
                    e.setStatus(dn.getStatus());
                    e.setEventDate(dn.getDeliveryDate() != null
                            ? dn.getDeliveryDate().toString() : null);
                    StringBuilder desc = new StringBuilder();
                    if (dn.getCustomerName() != null) desc.append("Customer: ").append(dn.getCustomerName());
                    if (dn.getCarrierName() != null) desc.append("  |  Carrier: ").append(dn.getCarrierName());
                    if (dn.getTrackingNumber() != null) desc.append("  |  Tracking: ").append(dn.getTrackingNumber());
                    e.setDescription(desc.toString().trim());
                });

        if (e.getEventDate() == null && m.getMovementDate() != null)
            e.setEventDate(m.getMovementDate().toString());
        if (e.getTitle() == null)
            e.setTitle("Sales Dispatch: " + (m.getReferenceNumber() != null ? m.getReferenceNumber() : "—"));
        else
            e.setTitle("Sales Dispatch: " + m.getReferenceNumber());
        return e;
    }

    private LotHistoryEventDto toSalesReturnEvent(LotMovement m, UUID tenantId) {
        LotHistoryEventDto e = new LotHistoryEventDto();
        e.setEventType("SALES_RETURN");
        e.setReferenceId(m.getReferenceId());
        e.setReferenceNumber(m.getReferenceNumber());
        e.setQuantity(m.getQuantity() != null ? m.getQuantity().abs() : null);

        salesReturnRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, m.getReferenceId())
                .ifPresent(ret -> {
                    e.setPartyName(ret.getCustomerName());
                    e.setStatus(ret.getStatus());
                    e.setEventDate(ret.getReturnDate() != null
                            ? ret.getReturnDate().toString() : null);
                    StringBuilder desc = new StringBuilder();
                    if (ret.getCustomerName() != null) desc.append("Customer: ").append(ret.getCustomerName());
                    if (ret.getReason() != null) desc.append("  |  Reason: ").append(ret.getReason());
                    e.setDescription(desc.toString().trim());
                });

        if (e.getEventDate() == null && m.getMovementDate() != null)
            e.setEventDate(m.getMovementDate().toString());
        if (e.getTitle() == null)
            e.setTitle("Sales Return: " + (m.getReferenceNumber() != null ? m.getReferenceNumber() : "—"));
        else
            e.setTitle("Sales Return: " + m.getReferenceNumber());
        return e;
    }

    private LotHistoryEventDto toProductionJobEvent(ProductionJob job) {
        LotHistoryEventDto e = new LotHistoryEventDto();
        boolean isProcess = job.getProcessType() != null && !job.getProcessType().isBlank();
        e.setEventType(isProcess ? "PROCESS_JOB" : "PRODUCTION_JOB");
        e.setProcessType(job.getProcessType());

        LocalDate date = job.getActualEndDate() != null ? job.getActualEndDate()
                       : job.getPlannedEndDate() != null ? job.getPlannedEndDate()
                       : job.getActualStartDate() != null ? job.getActualStartDate()
                       : job.getCreatedAt() != null ? job.getCreatedAt().toLocalDate() : null;
        e.setEventDate(date != null ? date.toString() : null);
        e.setReferenceNumber(job.getJobNumber());
        e.setReferenceId(job.getId());

        String typeLabel = isProcess ? job.getProcessType().replace('_', ' ') : "Production";
        e.setTitle(typeLabel + " Job: " + job.getJobNumber());
        e.setStatus(job.getStatus() != null ? job.getStatus().name() : null);
        e.setQuantity(job.getActualOutputQuantity() != null
                && job.getActualOutputQuantity().compareTo(BigDecimal.ZERO) > 0
                ? job.getActualOutputQuantity() : job.getPlannedOutputQuantity());
        e.setUnit(job.getOutputUnit());

        StringBuilder desc = new StringBuilder();
        if (job.getInputProductName() != null)
            desc.append("Input: ").append(job.getInputProductName());
        if (job.getOutputProductName() != null) {
            if (desc.length() > 0) desc.append(" → ");
            desc.append(job.getOutputProductName());
        }
        if (job.getSupervisor() != null)
            desc.append("  |  Supervisor: ").append(job.getSupervisor());
        if (job.getWorkCenter() != null)
            desc.append("  |  Work Centre: ").append(job.getWorkCenter());
        e.setDescription(desc.toString().trim());
        return e;
    }

    private LotHistoryEventDto toQualityEvent(QualityInspection qi) {
        LotHistoryEventDto e = new LotHistoryEventDto();
        e.setEventType("QUALITY_INSPECTION");
        e.setEventDate(qi.getInspectionDate() != null ? qi.getInspectionDate().toString() : null);
        e.setReferenceNumber(qi.getInspectionNumber());
        e.setReferenceId(qi.getId());
        e.setInspectionType(qi.getInspectionType());
        e.setInspectorName(qi.getInspectorName());
        e.setResult(qi.getResult());
        e.setStatus(qi.getStatus());

        String typeLabel = qi.getInspectionType() != null
                ? qi.getInspectionType().replace('_', ' ') : "Quality";
        e.setTitle(typeLabel + " Inspection: " + qi.getInspectionNumber());

        StringBuilder desc = new StringBuilder();
        if (qi.getInspectorName() != null)
            desc.append("Inspector: ").append(qi.getInspectorName());
        if (qi.getSampleSize() != null)
            desc.append("  |  Sample: ").append(qi.getSampleSize());
        if (qi.getDefectsFound() != null)
            desc.append("  |  Defects: ").append(qi.getDefectsFound());
        if (qi.getRemarks() != null && !qi.getRemarks().isBlank())
            desc.append("  |  ").append(qi.getRemarks());
        e.setDescription(desc.toString().trim());
        return e;
    }

    // ── Enriched movement DTO ────────────────────────────────────────────────

    private LotMovementDto toEnrichedMovementDto(LotMovement m, UUID tenantId) {
        LotMovementDto dto = toMovementDto(m);
        if ("DELIVERY_NOTE".equals(m.getReferenceType()) && m.getReferenceId() != null) {
            deliveryNoteRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, m.getReferenceId())
                    .ifPresent(dn -> {
                        dto.setPartyName(dn.getCustomerName());
                        if (dn.getCarrierName() != null)
                            dto.setExtraInfo("Carrier: " + dn.getCarrierName());
                    });
        } else if ("SALES_RETURN".equals(m.getReferenceType()) && m.getReferenceId() != null) {
            salesReturnRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, m.getReferenceId())
                    .ifPresent(ret -> {
                        dto.setPartyName(ret.getCustomerName());
                        if (ret.getReason() != null)
                            dto.setExtraInfo("Reason: " + ret.getReason());
                    });
        }
        return dto;
    }

    // ── Existing operations ──────────────────────────────────────────────────

    public LotStockDto getLotById(UUID id) {
        return toDto(findOrThrow(id));
    }

    public PageResponse<LotMovementDto> getLotMovements(String lotNumber, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = lotMovementRepository.findByTenantIdAndLotNumberAndDeletedAtIsNull(tenantId, lotNumber, pageable);
        return PageResponse.of(page.map(this::toMovementDto));
    }

    @Transactional
    public LotStockDto adjustLot(UUID lotId, BigDecimal qty, String reason) {
        UUID tenantId = tenantContext.current();
        LotStock lot = findOrThrow(lotId);

        BigDecimal balanceBefore = lot.getQuantityOnHand();
        BigDecimal balanceAfter = balanceBefore.add(qty);

        if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
            throw AppException.insufficientStock("Adjustment would result in negative stock for lot: " + lot.getLotNumber());
        }

        lot.setQuantityOnHand(balanceAfter);
        if (balanceAfter.compareTo(BigDecimal.ZERO) == 0) {
            lot.setStatus(LotStockStatus.EXHAUSTED);
        }

        LotMovement movement = new LotMovement();
        movement.setTenantId(tenantId);
        movement.setLotNumber(lot.getLotNumber());
        movement.setProductId(lot.getProductId());
        movement.setProductName(lot.getProductName());
        movement.setWarehouseId(lot.getWarehouseId());
        movement.setMovementType(LotMovType.ADJUSTMENT);
        movement.setMovementDate(LocalDate.now());
        movement.setQuantity(qty);
        movement.setBalanceBefore(balanceBefore);
        movement.setBalanceAfter(balanceAfter);
        movement.setReferenceType("MANUAL_ADJUSTMENT");
        movement.setNotes(reason);

        lotMovementRepository.save(movement);
        LotStock saved = lotStockRepository.save(lot);
        log.info("LotStock adjusted: id={}, lotNumber={}, qty={}", saved.getId(), saved.getLotNumber(), qty);
        return toDto(saved);
    }

    @Transactional
    public LotStockDto quarantineLot(UUID lotId, String reason) {
        LotStock lot = findOrThrow(lotId);
        if (lot.getStatus() == LotStockStatus.EXHAUSTED || lot.getStatus() == LotStockStatus.EXPIRED) {
            throw AppException.badRequest("Cannot quarantine an exhausted or expired lot");
        }

        BigDecimal balanceBefore = lot.getQuantityOnHand();
        lot.setStatus(LotStockStatus.QUARANTINE);

        LotMovement movement = new LotMovement();
        movement.setTenantId(tenantContext.current());
        movement.setLotNumber(lot.getLotNumber());
        movement.setProductId(lot.getProductId());
        movement.setProductName(lot.getProductName());
        movement.setWarehouseId(lot.getWarehouseId());
        movement.setMovementType(LotMovType.QUARANTINE);
        movement.setMovementDate(LocalDate.now());
        movement.setQuantity(balanceBefore);
        movement.setBalanceBefore(balanceBefore);
        movement.setBalanceAfter(balanceBefore);
        movement.setReferenceType("QUARANTINE");
        movement.setNotes(reason);

        lotMovementRepository.save(movement);
        LotStock saved = lotStockRepository.save(lot);
        log.info("LotStock quarantined: id={}, lotNumber={}", saved.getId(), saved.getLotNumber());
        return toDto(saved);
    }

    @Transactional
    public LotStockDto releaseLot(UUID lotId) {
        LotStock lot = findOrThrow(lotId);
        if (lot.getStatus() != LotStockStatus.QUARANTINE) {
            throw AppException.badRequest("Only QUARANTINE lots can be released");
        }
        lot.setStatus(LotStockStatus.RELEASED);
        LotStock saved = lotStockRepository.save(lot);
        log.info("LotStock released: id={}, lotNumber={}", saved.getId(), saved.getLotNumber());
        return toDto(saved);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    public LotStockDto toDto(LotStock l) {
        LotStockDto dto = new LotStockDto();
        dto.setId(l.getId());
        dto.setTenantId(l.getTenantId());
        dto.setLotNumber(l.getLotNumber());
        dto.setProductId(l.getProductId());
        dto.setProductName(l.getProductName());
        dto.setWarehouseId(l.getWarehouseId());
        dto.setWarehouseName(l.getWarehouseName());
        dto.setStorageLocationId(l.getStorageLocationId());
        dto.setStorageLocationName(l.getStorageLocationName());
        dto.setSourceType(l.getSourceType());
        dto.setSourceId(l.getSourceId());
        dto.setProductionDate(l.getProductionDate());
        dto.setExpiryDate(l.getExpiryDate());
        dto.setQuantityOnHand(l.getQuantityOnHand());
        dto.setQuantityReserved(l.getQuantityReserved());
        dto.setUnitCost(l.getUnitCost());
        dto.setStatus(l.getStatus());
        dto.setCreatedAt(l.getCreatedAt());
        return dto;
    }

    public LotMovementDto toMovementDto(LotMovement m) {
        LotMovementDto dto = new LotMovementDto();
        dto.setId(m.getId());
        dto.setTenantId(m.getTenantId());
        dto.setLotNumber(m.getLotNumber());
        dto.setProductId(m.getProductId());
        dto.setProductName(m.getProductName());
        dto.setWarehouseId(m.getWarehouseId());
        dto.setMovementType(m.getMovementType());
        dto.setMovementDate(m.getMovementDate());
        dto.setQuantity(m.getQuantity());
        dto.setBalanceBefore(m.getBalanceBefore());
        dto.setBalanceAfter(m.getBalanceAfter());
        dto.setReferenceType(m.getReferenceType());
        dto.setReferenceId(m.getReferenceId());
        dto.setReferenceNumber(m.getReferenceNumber());
        dto.setNotes(m.getNotes());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }

    private LotStock findOrThrow(UUID id) {
        return lotStockRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Lot stock not found: " + id));
    }
}

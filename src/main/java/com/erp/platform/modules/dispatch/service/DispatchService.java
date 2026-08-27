package com.erp.platform.modules.dispatch.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.dispatch.dto.CreateDispatchRequest;
import com.erp.platform.modules.dispatch.dto.DispatchDto;
import com.erp.platform.modules.dispatch.entity.Dispatch;
import com.erp.platform.modules.dispatch.entity.Dispatch.DispatchStatus;
import com.erp.platform.modules.dispatch.entity.DispatchItem;
import com.erp.platform.modules.agri.repository.LotStatusRecordRepository;
import com.erp.platform.modules.dispatch.repository.DispatchRepository;
import com.erp.platform.modules.inventory.entity.LotStock.LotStockStatus;
import com.erp.platform.modules.inventory.repository.LotStockRepository;
import com.erp.platform.modules.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final LotStockRepository lotStockRepository;
    private final LotStatusRecordRepository lotStatusRecordRepository;
    private final StockService stockService;
    /** Picks the lots a dispatch draws from, soonest expiry first. */
    private final com.erp.platform.modules.inventory.service.FefoService fefoService;
    private final TenantContext tenantContext;

    public PageResponse<DispatchDto> list(DispatchStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = status != null
                ? dispatchRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : dispatchRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public DispatchDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public DispatchDto create(CreateDispatchRequest request) {
        UUID tenantId = tenantContext.current();

        Dispatch dispatch = new Dispatch();
        dispatch.setTenantId(tenantId);
        dispatch.setDispatchNumber(generateDispatchNumber(tenantId));
        dispatch.setDispatchType(request.getDispatchType());
        dispatch.setStatus(DispatchStatus.DRAFT);
        dispatch.setSalesOrderId(request.getSalesOrderId());
        dispatch.setCustomerId(request.getCustomerId());
        dispatch.setCustomerName(request.getCustomerName());
        dispatch.setDeliveryOrderRef(request.getDeliveryOrderRef());
        dispatch.setChallanValue(request.getChallanValue() != null ? request.getChallanValue() : java.math.BigDecimal.ZERO);
        dispatch.setBillingAddress(request.getBillingAddress());
        dispatch.setBillingPhone(request.getBillingPhone());
        dispatch.setDeliveryAddress(request.getDeliveryAddress());
        dispatch.setDeliveryPhone(request.getDeliveryPhone());
        dispatch.setConsigneeBuyerSame(request.isConsigneeBuyerSame());
        dispatch.setSalesOffice(request.getSalesOffice());
        dispatch.setFromLocation(request.getFromLocation());
        dispatch.setToLocation(request.getToLocation());
        dispatch.setStockTransferOrderRef(request.getStockTransferOrderRef());
        dispatch.setDispatchDate(request.getDispatchDate() != null ? request.getDispatchDate() : LocalDate.now());
        dispatch.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        dispatch.setCarrierName(request.getCarrierName());
        dispatch.setCarrierPhone(request.getCarrierPhone());
        dispatch.setVehicleNumber(request.getVehicleNumber());
        dispatch.setTrackingNumber(request.getTrackingNumber());
        dispatch.setLrNumber(request.getLrNumber());
        dispatch.setWayBillNumber(request.getWayBillNumber());
        dispatch.setRrRlNumber(request.getRrRlNumber());
        dispatch.setFreightCharges(request.getFreightCharges() != null ? request.getFreightCharges() : java.math.BigDecimal.ZERO);
        dispatch.setFreightPaidAdvance(request.getFreightPaidAdvance() != null ? request.getFreightPaidAdvance() : java.math.BigDecimal.ZERO);
        dispatch.setFreightToPay(request.getFreightToPay() != null ? request.getFreightToPay() : java.math.BigDecimal.ZERO);
        dispatch.setBalanceAfterSubmission(request.getBalanceAfterSubmission() != null ? request.getBalanceAfterSubmission() : java.math.BigDecimal.ZERO);
        dispatch.setTotalPackages(request.getTotalPackages());
        dispatch.setTotalWeight(request.getTotalWeight() != null ? request.getTotalWeight() : java.math.BigDecimal.ZERO);
        dispatch.setWeightUnit(request.getWeightUnit() != null ? request.getWeightUnit() : "KG");
        dispatch.setPackedBy(request.getPackedBy());
        dispatch.setTransporterPanNumber(request.getTransporterPanNumber());
        dispatch.setDocumentsThrough(request.getDocumentsThrough());
        dispatch.setNotes(request.getNotes());

        List<DispatchItem> items = buildItems(dispatch, tenantId, request.getItems());
        dispatch.setItems(items);

        dispatch = dispatchRepository.save(dispatch);
        log.info("Dispatch created: id={}, number={}", dispatch.getId(), dispatch.getDispatchNumber());
        return toDto(dispatch);
    }

    @Transactional
    public DispatchDto update(UUID id, CreateDispatchRequest request) {
        Dispatch dispatch = findOrThrow(id);

        dispatch.setDispatchType(request.getDispatchType());
        dispatch.setSalesOrderId(request.getSalesOrderId());
        dispatch.setCustomerId(request.getCustomerId());
        dispatch.setCustomerName(request.getCustomerName());
        dispatch.setDeliveryOrderRef(request.getDeliveryOrderRef());
        dispatch.setChallanValue(request.getChallanValue() != null ? request.getChallanValue() : java.math.BigDecimal.ZERO);
        dispatch.setBillingAddress(request.getBillingAddress());
        dispatch.setBillingPhone(request.getBillingPhone());
        dispatch.setDeliveryAddress(request.getDeliveryAddress());
        dispatch.setDeliveryPhone(request.getDeliveryPhone());
        dispatch.setConsigneeBuyerSame(request.isConsigneeBuyerSame());
        dispatch.setSalesOffice(request.getSalesOffice());
        dispatch.setFromLocation(request.getFromLocation());
        dispatch.setToLocation(request.getToLocation());
        dispatch.setStockTransferOrderRef(request.getStockTransferOrderRef());
        dispatch.setDispatchDate(request.getDispatchDate());
        dispatch.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        dispatch.setCarrierName(request.getCarrierName());
        dispatch.setCarrierPhone(request.getCarrierPhone());
        dispatch.setVehicleNumber(request.getVehicleNumber());
        dispatch.setTrackingNumber(request.getTrackingNumber());
        dispatch.setLrNumber(request.getLrNumber());
        dispatch.setWayBillNumber(request.getWayBillNumber());
        dispatch.setRrRlNumber(request.getRrRlNumber());
        dispatch.setFreightCharges(request.getFreightCharges() != null ? request.getFreightCharges() : java.math.BigDecimal.ZERO);
        dispatch.setFreightPaidAdvance(request.getFreightPaidAdvance() != null ? request.getFreightPaidAdvance() : java.math.BigDecimal.ZERO);
        dispatch.setFreightToPay(request.getFreightToPay() != null ? request.getFreightToPay() : java.math.BigDecimal.ZERO);
        dispatch.setBalanceAfterSubmission(request.getBalanceAfterSubmission() != null ? request.getBalanceAfterSubmission() : java.math.BigDecimal.ZERO);
        dispatch.setTotalPackages(request.getTotalPackages());
        dispatch.setTotalWeight(request.getTotalWeight() != null ? request.getTotalWeight() : java.math.BigDecimal.ZERO);
        dispatch.setWeightUnit(request.getWeightUnit() != null ? request.getWeightUnit() : "KG");
        dispatch.setPackedBy(request.getPackedBy());
        dispatch.setTransporterPanNumber(request.getTransporterPanNumber());
        dispatch.setDocumentsThrough(request.getDocumentsThrough());
        dispatch.setNotes(request.getNotes());

        if (request.getItems() != null) {
            dispatch.getItems().clear();
            List<DispatchItem> items = buildItems(dispatch, dispatch.getTenantId(), request.getItems());
            dispatch.getItems().addAll(items);
        }

        dispatch = dispatchRepository.save(dispatch);
        log.info("Dispatch updated: id={}", dispatch.getId());
        return toDto(dispatch);
    }

    @Transactional
    public DispatchDto updateStatus(UUID id, DispatchStatus status) {
        Dispatch dispatch = findOrThrow(id);
        validateTransition(dispatch.getStatus(), status);

        // Quality gate — check before persisting; two tiers:
        // 1. QUARANTINE status in LotStock (explicit hold placed by QC team)
        // 2. qualityStatus != PASS in LotStatusRecord (lot was evaluated and explicitly failed)
        if (status == DispatchStatus.DISPATCHED && dispatch.getItems() != null) {
            UUID tenantId = tenantContext.current();
            for (DispatchItem item : dispatch.getItems()) {
                if (item.getLotNumber() == null || item.getLotNumber().isBlank()) continue;
                boolean quarantined = lotStockRepository
                        .findByTenantIdAndLotNumberAndDeletedAtIsNull(tenantId, item.getLotNumber())
                        .stream()
                        .anyMatch(lot -> lot.getStatus() == LotStockStatus.QUARANTINE);
                if (quarantined) {
                    throw AppException.badRequest(
                            "Lot " + item.getLotNumber() + " is under quarantine and cannot be dispatched. " +
                            "Release the lot from quality hold before dispatch.");
                }
                lotStatusRecordRepository
                        .findByTenantIdAndLotNumberAndDeletedAtIsNull(tenantId, item.getLotNumber())
                        .stream()
                        .filter(r -> r.getQualityStatus() != null && !"PASS".equalsIgnoreCase(r.getQualityStatus()))
                        .findFirst()
                        .ifPresent(r -> { throw AppException.badRequest(
                                "Lot " + item.getLotNumber() + " has quality status '" + r.getQualityStatus() +
                                "' and cannot be dispatched. Only lots with quality status PASS are eligible for dispatch."); });
            }
        }

        dispatch.setStatus(status);
        if (status == DispatchStatus.DELIVERED) {
            dispatch.setActualDeliveryDate(LocalDate.now());
        }
        dispatch = dispatchRepository.save(dispatch);

        // Auto-deduct inventory when goods leave warehouse
        if (status == DispatchStatus.DISPATCHED) {
            final UUID dispId = dispatch.getId();
            final String dispNumber = dispatch.getDispatchNumber();
            if (dispatch.getItems() != null) {
                for (DispatchItem item : dispatch.getItems()) {
                    if (item.getProductId() != null && item.getQuantity() != null
                            && item.getQuantity().compareTo(java.math.BigDecimal.ZERO) > 0) {
                        try {
                            stockService.deductStock(item.getProductId(), item.getWarehouseId(), item.getQuantity(),
                                    "DISPATCH", dispId, dispNumber,
                                    "PACKED", "GOOD", item.getLotNumber(), "DISPATCH");
                        } catch (Exception e) {
                            log.warn("Stock deduction skipped for product {}: {}", item.getProductId(), e.getMessage());
                        }
                        // Dispatch named a product, not a lot, so the aggregate deduction above left
                        // the lot balances untouched — stock left the building without any record of
                        // which lot it came from, and nothing stopped the oldest stock expiring on
                        // the shelf while newer stock shipped. FEFO picks soonest-expiry-first and
                        // writes an ISSUE movement per lot, so a dispatch is traceable to the lots
                        // that fulfilled it. Products with no lots at all are skipped quietly; a
                        // genuine shortfall on a lot-tracked product throws and rolls the dispatch
                        // back rather than shipping short.
                        fefoService.consumeIfLotTracked(item.getProductId(), item.getWarehouseId(),
                                item.getQuantity(), "DISPATCH", dispId, dispNumber);
                    }
                }
            }
        }

        log.info("Dispatch status updated: id={}, status={}", dispatch.getId(), status);
        return toDto(dispatch);
    }

    @Transactional
    public DispatchDto recordPOD(UUID id, LocalDate podDate, String podReference) {
        Dispatch dispatch = findOrThrow(id);
        dispatch.setPodReceived(true);
        dispatch.setPodDate(podDate != null ? podDate : LocalDate.now());
        dispatch.setPodReference(podReference);
        if (dispatch.getStatus() != DispatchStatus.DELIVERED) {
            dispatch.setStatus(DispatchStatus.DELIVERED);
            dispatch.setActualDeliveryDate(LocalDate.now());
        }
        dispatch = dispatchRepository.save(dispatch);
        log.info("POD recorded for dispatch: id={}", dispatch.getId());
        return toDto(dispatch);
    }

    @Transactional
    public void delete(UUID id) {
        Dispatch dispatch = findOrThrow(id);
        dispatch.setDeletedAt(LocalDateTime.now());
        dispatchRepository.save(dispatch);
        log.info("Dispatch soft-deleted: id={}", id);
    }

    private List<DispatchItem> buildItems(Dispatch dispatch, UUID tenantId,
            List<CreateDispatchRequest.ItemRequest> requests) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            DispatchItem item = new DispatchItem();
            item.setTenantId(tenantId);
            item.setDispatch(dispatch);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setWarehouseId(r.getWarehouseId());
            item.setLotNumber(r.getLotNumber());
            item.setBatchNumber(r.getBatchNumber());
            item.setQuantity(r.getQuantity());
            item.setUnit(r.getUnit());
            item.setPackageCount(r.getPackageCount() > 0 ? r.getPackageCount() : 1);
            item.setPackageType(r.getPackageType());
            item.setPackSizeKg(r.getPackSizeKg() != null ? r.getPackSizeKg() : java.math.BigDecimal.ZERO);
            item.setSecondaryPackType(r.getSecondaryPackType());
            item.setSecondaryPackCount(r.getSecondaryPackCount());
            item.setSecondaryBagSize(r.getSecondaryBagSize());
            item.setMaterialState(r.getMaterialState());
            item.setMaterialType(r.getMaterialType());
            item.setUnitPrice(r.getUnitPrice() != null ? r.getUnitPrice() : java.math.BigDecimal.ZERO);
            item.setGrossWeight(r.getGrossWeight());
            item.setNetWeight(r.getNetWeight());
            item.setRemarks(r.getRemarks());
            return item;
        }).collect(Collectors.toList());
    }

    private DispatchDto toDto(Dispatch d) {
        DispatchDto dto = new DispatchDto();
        dto.setId(d.getId());
        dto.setTenantId(d.getTenantId());
        dto.setDispatchNumber(d.getDispatchNumber());
        dto.setDispatchType(d.getDispatchType());
        dto.setStatus(d.getStatus());
        dto.setSalesOrderId(d.getSalesOrderId());
        dto.setCustomerId(d.getCustomerId());
        dto.setCustomerName(d.getCustomerName());
        dto.setDeliveryOrderRef(d.getDeliveryOrderRef());
        dto.setChallanValue(d.getChallanValue());
        dto.setBillingAddress(d.getBillingAddress());
        dto.setBillingPhone(d.getBillingPhone());
        dto.setDeliveryAddress(d.getDeliveryAddress());
        dto.setDeliveryPhone(d.getDeliveryPhone());
        dto.setConsigneeBuyerSame(Boolean.TRUE.equals(d.getConsigneeBuyerSame()));
        dto.setSalesOffice(d.getSalesOffice());
        dto.setFromLocation(d.getFromLocation());
        dto.setToLocation(d.getToLocation());
        dto.setStockTransferOrderRef(d.getStockTransferOrderRef());
        dto.setDispatchDate(d.getDispatchDate());
        dto.setExpectedDeliveryDate(d.getExpectedDeliveryDate());
        dto.setActualDeliveryDate(d.getActualDeliveryDate());
        dto.setCarrierName(d.getCarrierName());
        dto.setCarrierPhone(d.getCarrierPhone());
        dto.setVehicleNumber(d.getVehicleNumber());
        dto.setTrackingNumber(d.getTrackingNumber());
        dto.setLrNumber(d.getLrNumber());
        dto.setWayBillNumber(d.getWayBillNumber());
        dto.setRrRlNumber(d.getRrRlNumber());
        dto.setFreightCharges(d.getFreightCharges());
        dto.setFreightPaidAdvance(d.getFreightPaidAdvance());
        dto.setFreightToPay(d.getFreightToPay());
        dto.setBalanceAfterSubmission(d.getBalanceAfterSubmission());
        dto.setTotalPackages(d.getTotalPackages());
        dto.setTotalWeight(d.getTotalWeight());
        dto.setWeightUnit(d.getWeightUnit());
        dto.setPackedBy(d.getPackedBy());
        dto.setTransporterPanNumber(d.getTransporterPanNumber());
        dto.setDocumentsThrough(d.getDocumentsThrough());
        dto.setPodReceived(Boolean.TRUE.equals(d.getPodReceived()));
        dto.setPodDate(d.getPodDate());
        dto.setPodReference(d.getPodReference());
        dto.setNotes(d.getNotes());
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        if (d.getItems() != null) {
            dto.setItems(d.getItems().stream().map(item -> {
                DispatchDto.ItemDto idto = new DispatchDto.ItemDto();
                idto.setId(item.getId());
                idto.setProductId(item.getProductId());
                idto.setProductName(item.getProductName());
                idto.setWarehouseId(item.getWarehouseId());
                idto.setLotNumber(item.getLotNumber());
                idto.setBatchNumber(item.getBatchNumber());
                idto.setQuantity(item.getQuantity());
                idto.setUnit(item.getUnit());
                idto.setPackageCount(item.getPackageCount());
                idto.setPackageType(item.getPackageType());
                idto.setPackSizeKg(item.getPackSizeKg());
                idto.setSecondaryPackType(item.getSecondaryPackType());
                idto.setSecondaryPackCount(item.getSecondaryPackCount());
                idto.setSecondaryBagSize(item.getSecondaryBagSize());
                idto.setMaterialState(item.getMaterialState());
                idto.setMaterialType(item.getMaterialType());
                idto.setUnitPrice(item.getUnitPrice());
                idto.setGrossWeight(item.getGrossWeight());
                idto.setNetWeight(item.getNetWeight());
                idto.setRemarks(item.getRemarks());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private static final Map<DispatchStatus, Set<DispatchStatus>> ALLOWED_TRANSITIONS = Map.of(
        DispatchStatus.DRAFT,      EnumSet.of(DispatchStatus.PACKED, DispatchStatus.DISPATCHED, DispatchStatus.RETURNED),
        DispatchStatus.PACKED,     EnumSet.of(DispatchStatus.DISPATCHED, DispatchStatus.RETURNED),
        DispatchStatus.DISPATCHED, EnumSet.of(DispatchStatus.IN_TRANSIT, DispatchStatus.DELIVERED, DispatchStatus.RETURNED),
        DispatchStatus.IN_TRANSIT, EnumSet.of(DispatchStatus.DELIVERED, DispatchStatus.RETURNED),
        DispatchStatus.DELIVERED,  EnumSet.noneOf(DispatchStatus.class),
        DispatchStatus.RETURNED,   EnumSet.noneOf(DispatchStatus.class)
    );

    private void validateTransition(DispatchStatus from, DispatchStatus to) {
        Set<DispatchStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(DispatchStatus.class));
        if (!allowed.contains(to)) {
            throw AppException.badRequest(
                    "Invalid status transition: " + from + " → " + to +
                    ". Allowed: " + allowed);
        }
    }

    private Dispatch findOrThrow(UUID id) {
        return dispatchRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Dispatch not found: " + id));
    }

    private String generateDispatchNumber(UUID tenantId) {
        long count = dispatchRepository.countByTenantId(tenantId);
        String year = String.valueOf(Year.now().getValue());
        return String.format("DSP-%s-%03d", year, count + 1);
    }
}

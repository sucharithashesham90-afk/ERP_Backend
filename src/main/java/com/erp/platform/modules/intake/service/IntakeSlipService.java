package com.erp.platform.modules.intake.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.intake.dto.CreateIntakeSlipRequest;
import com.erp.platform.modules.intake.dto.IntakeSlipDto;
import com.erp.platform.modules.intake.entity.IntakeSchedule;
import com.erp.platform.modules.intake.entity.IntakeScheduleItem;
import com.erp.platform.modules.intake.entity.IntakeSlip;
import com.erp.platform.modules.intake.entity.IntakeSlip.SlipStatus;
import com.erp.platform.modules.intake.entity.IntakeSlipItem;
import com.erp.platform.modules.intake.repository.IntakeScheduleRepository;
import com.erp.platform.modules.intake.repository.IntakeSlipRepository;
import com.erp.platform.modules.inventory.service.StockService;
import com.erp.platform.modules.purchase.entity.GoodsReceipt;
import com.erp.platform.modules.purchase.entity.GoodsReceiptItem;
import com.erp.platform.modules.purchase.entity.PurchaseOrder;
import com.erp.platform.modules.purchase.repository.GoodsReceiptRepository;
import com.erp.platform.modules.purchase.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IntakeSlipService {

    private final IntakeSlipRepository intakeSlipRepository;
    private final IntakeScheduleRepository intakeScheduleRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockService stockService;
    private final com.erp.platform.modules.purchase.service.IntakeLiabilityService intakeLiabilityService;
    private final TenantContext tenantContext;

    public PageResponse<IntakeSlipDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(
                intakeSlipRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public IntakeSlipDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public IntakeSlipDto create(CreateIntakeSlipRequest request) {
        UUID tenantId = tenantContext.current();

        IntakeSlip slip = new IntakeSlip();
        slip.setTenantId(tenantId);
        slip.setSlipNumber(generateSlipNumber(tenantId));
        slip.setPurchaseOrderId(request.getPurchaseOrderId());
        slip.setVendorId(request.getVendorId());
        slip.setVendorName(request.getVendorName());
        slip.setReceiptDate(request.getReceiptDate() != null ? request.getReceiptDate() : LocalDate.now());
        slip.setStatus(SlipStatus.DRAFT);
        slip.setVehicleNumber(request.getVehicleNumber());
        slip.setDriverName(request.getDriverName());
        slip.setTotalQuantity(BigDecimal.ZERO);
        slip.setAcceptedQuantity(BigDecimal.ZERO);
        slip.setRejectedQuantity(BigDecimal.ZERO);
        slip.setWarehouseId(request.getWarehouseId());
        slip.setWarehouseName(request.getWarehouseName());
        slip.setNotes(request.getNotes());

        // Agricultural lot fields
        slip.setLocation(request.getLocation());
        slip.setDeliveryType(request.getDeliveryType());
        slip.setTcName(request.getTcName());
        slip.setFieldProducerId(request.getFieldProducerId());
        slip.setFieldProducerName(request.getFieldProducerName());
        slip.setPlantVariantId(request.getPlantVariantId());
        slip.setPlantVariantName(request.getPlantVariantName());
        slip.setTemporaryLotNumber(request.getTemporaryLotNumber());
        slip.setFatherName(request.getFatherName());
        slip.setVillage(request.getVillage());
        slip.setInwardGatePassNumber(request.getInwardGatePassNumber());
        slip.setWeighbridgeQuantity(request.getWeighbridgeQuantity());
        slip.setGodownName(request.getGodownName() != null ? request.getGodownName() : request.getWarehouseName());
        slip.setSupervisorName(request.getSupervisorName());
        slip.setStackNumber(request.getStackNumber());
        slip.setHamaliContractor(request.getHamaliContractor());
        slip.setEmptyGunnyWeightKg(request.getEmptyGunnyWeightKg());
        slip.setLotNumber(request.getLotNumber());
        slip.setInputType(request.getInputType());
        slip.setMoisturePercent(request.getMoisturePercent());
        slip.setUnloadingSlipNumber(request.getUnloadingSlipNumber());
        slip.setBagWeightMode(request.getBagWeightMode());
        slip.setBagType(request.getBagType());
        slip.setBagSizeKg(request.getBagSizeKg());
        slip.setQuantityPerBag(request.getQuantityPerBag());
        slip.setNumberOfBags(request.getNumberOfBags());

        if (request.getScheduleId() != null) {
            IntakeSchedule schedule = intakeScheduleRepository
                    .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getScheduleId())
                    .orElseThrow(() -> AppException.notFound("Intake schedule not found: " + request.getScheduleId()));
            slip.setSchedule(schedule);
        }

        List<IntakeSlipItem> items = buildItems(slip, tenantId, request.getItems());
        slip.setItems(items);

        BigDecimal totalQty = items.stream()
                .map(IntakeSlipItem::getReceivedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        slip.setTotalQuantity(totalQty);

        slip = intakeSlipRepository.save(slip);
        log.info("IntakeSlip created: id={}, number={}", slip.getId(), slip.getSlipNumber());
        return toDto(slip);
    }

    @Transactional
    public IntakeSlipDto updateStatus(UUID id, SlipStatus status) {
        IntakeSlip slip = findOrThrow(id);
        slip.setStatus(status);
        intakeSlipRepository.save(slip);

        if (status == SlipStatus.COMPLETED) {
            try {
                createGoodsReceiptForSlip(slip);
            } catch (Exception e) {
                log.warn("Goods receipt generation skipped for intake slip {}: {}",
                        slip.getSlipNumber(), e.getMessage());
            }

            // Completing an intake is the moment the grower becomes owed. Until now nothing wrote
            // that down, so the Liability Payment screen had nothing to pay and the chain from
            // intake to voucher simply stopped here.
            try {
                intakeLiabilityService.raiseFor(slip);
            } catch (Exception e) {
                // Never the reason an intake cannot be completed: the goods have arrived either way,
                // and the liability can be raised again by re-completing or from the backfill.
                log.warn("Liability not raised for intake slip {}: {}",
                        slip.getSlipNumber(), e.getMessage());
            }
        }

        if (status == SlipStatus.COMPLETED && slip.getWarehouseId() != null) {
            List<IntakeSlipItem> items = slip.getItems();
            if (items != null) {
                for (IntakeSlipItem item : items) {
                    if (item.getProductId() == null) continue;
                    BigDecimal qty = item.getAcceptedQuantity() != null
                            && item.getAcceptedQuantity().compareTo(BigDecimal.ZERO) > 0
                            ? item.getAcceptedQuantity()
                            : item.getReceivedQuantity();
                    if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) continue;
                    try {
                        stockService.addStock(item.getProductId(), slip.getWarehouseId(), qty,
                                "INTAKE_SLIP", slip.getId(), slip.getSlipNumber(), item.getProductName(),
                                "RAW", "GOOD", item.getLotNumber(), "INTAKE");
                        log.info("Stock added from IntakeSlip {}: product={}, qty={}", slip.getSlipNumber(), item.getProductId(), qty);
                    } catch (Exception e) {
                        log.warn("Stock update skipped for intake item product={}: {}", item.getProductId(), e.getMessage());
                    }
                }
            }
        }

        return toDto(slip);
    }

    /**
     * Raise a goods receipt for a completed intake slip and link it to the purchase order the
     * intake was made against, so the receipt shows up in the three-way match alongside the PO
     * and the invoice.
     *
     * The purchase order is taken from the slip, falling back to the purchase order recorded on
     * the intake schedule's lines. With neither there is nothing to match against, so no receipt
     * is raised.
     *
     * Stock is deliberately NOT posted here — completing the slip already does that, and posting
     * again from the receipt would double-count the intake.
     */
    private void createGoodsReceiptForSlip(IntakeSlip slip) {
        UUID tenantId = slip.getTenantId();
        if (goodsReceiptRepository.existsByTenantIdAndSourceIntakeSlipIdAndDeletedAtIsNull(tenantId, slip.getId())) {
            return;   // already raised for this slip
        }

        UUID purchaseOrderId = resolvePurchaseOrderId(slip);
        if (purchaseOrderId == null) {
            log.debug("Intake slip {} is not against a purchase order — no goods receipt raised",
                    slip.getSlipNumber());
            return;
        }
        PurchaseOrder po = purchaseOrderRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, purchaseOrderId)
                .orElse(null);
        if (po == null) {
            log.warn("Intake slip {} references purchase order {} which no longer exists",
                    slip.getSlipNumber(), purchaseOrderId);
            return;
        }

        GoodsReceipt grn = new GoodsReceipt();
        grn.setTenantId(tenantId);
        grn.setGrnNumber("GRN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        grn.setPurchaseOrderId(po.getId());
        grn.setSourceIntakeSlipId(slip.getId());
        grn.setVendorId(po.getVendorId() != null ? po.getVendorId() : slip.getVendorId());
        grn.setVendorName(po.getVendorName() != null ? po.getVendorName() : slip.getVendorName());
        grn.setWarehouseId(slip.getWarehouseId());
        grn.setReceiptDate(slip.getReceiptDate() != null ? slip.getReceiptDate() : LocalDate.now());
        grn.setVehicleNumber(slip.getVehicleNumber());
        grn.setDriverName(slip.getDriverName());
        grn.setInGatePass(slip.getInwardGatePassNumber());
        grn.setStatus("RECEIVED");
        grn.setNotes("Auto-generated from intake slip " + slip.getSlipNumber());

        List<GoodsReceiptItem> grnItems = new ArrayList<>();
        for (IntakeSlipItem si : slip.getItems() == null ? List.<IntakeSlipItem>of() : slip.getItems()) {
            if (si.getProductId() == null) continue;
            BigDecimal received = si.getReceivedQuantity() != null ? si.getReceivedQuantity() : BigDecimal.ZERO;
            BigDecimal accepted = si.getAcceptedQuantity() != null ? si.getAcceptedQuantity() : received;
            BigDecimal rejected = si.getRejectedQuantity() != null ? si.getRejectedQuantity()
                    : received.subtract(accepted).max(BigDecimal.ZERO);

            GoodsReceiptItem gi = new GoodsReceiptItem();
            gi.setGoodsReceipt(grn);
            gi.setProductId(si.getProductId());
            gi.setProductName(si.getProductName());
            gi.setReceivedQty(received);
            gi.setAcceptedQty(accepted);
            gi.setRejectedQty(rejected);
            gi.setBatchNumber(si.getLotNumber());
            grnItems.add(gi);
        }
        grn.setItems(grnItems);
        goodsReceiptRepository.save(grn);

        // Roll the accepted quantities onto the PO lines and move the PO's status on.
        for (GoodsReceiptItem gi : grnItems) {
            po.getItems().stream()
                    .filter(pi -> gi.getProductId().equals(pi.getProductId()))
                    .findFirst()
                    .ifPresent(pi -> {
                        BigDecimal already = pi.getReceivedQty() != null ? pi.getReceivedQty() : BigDecimal.ZERO;
                        pi.setReceivedQty(already.add(gi.getAcceptedQty()));
                    });
        }
        if (po.getStatus() != PurchaseOrder.POStatus.CANCELLED) {
            boolean allReceived = po.getItems().stream().allMatch(pi -> {
                BigDecimal received = pi.getReceivedQty() != null ? pi.getReceivedQty() : BigDecimal.ZERO;
                BigDecimal ordered = pi.getQuantity() != null ? pi.getQuantity() : BigDecimal.ZERO;
                return received.compareTo(ordered) >= 0;
            });
            po.setStatus(allReceived ? PurchaseOrder.POStatus.RECEIVED
                    : PurchaseOrder.POStatus.PARTIALLY_RECEIVED);
        }
        purchaseOrderRepository.save(po);

        log.info("Goods receipt {} raised from intake slip {} against PO {}",
                grn.getGrnNumber(), slip.getSlipNumber(), po.getPoNumber());
    }

    /** PO on the slip, else the first PO recorded on the intake schedule's lines. */
    private UUID resolvePurchaseOrderId(IntakeSlip slip) {
        if (slip.getPurchaseOrderId() != null) return slip.getPurchaseOrderId();
        if (slip.getSchedule() == null || slip.getSchedule().getItems() == null) return null;
        return slip.getSchedule().getItems().stream()
                .map(IntakeScheduleItem::getPurchaseOrderId)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void delete(UUID id) {
        IntakeSlip slip = findOrThrow(id);
        if (slip.getStatus() != SlipStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT slips can be deleted");
        }
        slip.setDeletedAt(LocalDateTime.now());
        intakeSlipRepository.save(slip);
        log.info("IntakeSlip soft-deleted: id={}", id);
    }

    private List<IntakeSlipItem> buildItems(IntakeSlip slip, UUID tenantId,
            List<CreateIntakeSlipRequest.ItemRequest> requests) {
        if (requests == null) return new ArrayList<>();
        AtomicInteger lotSeq = new AtomicInteger(1);
        String lotDatePrefix = "LOT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        return requests.stream().map(r -> {
            IntakeSlipItem item = new IntakeSlipItem();
            item.setTenantId(tenantId);
            item.setSlip(slip);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setReceivedQuantity(r.getReceivedQuantity() != null ? r.getReceivedQuantity() : BigDecimal.ZERO);
            item.setAcceptedQuantity(BigDecimal.ZERO);
            item.setRejectedQuantity(BigDecimal.ZERO);
            item.setUnit(r.getUnit());
            item.setUnitPrice(r.getUnitPrice() != null ? r.getUnitPrice() : BigDecimal.ZERO);
            item.setLotNumber(String.format("%s%03d", lotDatePrefix, lotSeq.getAndIncrement()));
            item.setQualityRemarks(r.getQualityRemarks());
            return item;
        }).collect(Collectors.toList());
    }

    private IntakeSlipDto toDto(IntakeSlip slip) {
        IntakeSlipDto dto = new IntakeSlipDto();
        dto.setId(slip.getId());
        dto.setTenantId(slip.getTenantId());
        dto.setSlipNumber(slip.getSlipNumber());
        if (slip.getSchedule() != null) {
            dto.setScheduleId(slip.getSchedule().getId());
            dto.setScheduleNumber(slip.getSchedule().getScheduleNumber());
        }
        dto.setPurchaseOrderId(slip.getPurchaseOrderId());
        dto.setVendorId(slip.getVendorId());
        dto.setVendorName(slip.getVendorName());
        dto.setReceiptDate(slip.getReceiptDate());
        dto.setStatus(slip.getStatus());
        dto.setVehicleNumber(slip.getVehicleNumber());
        dto.setDriverName(slip.getDriverName());
        dto.setTotalQuantity(slip.getTotalQuantity());
        dto.setAcceptedQuantity(slip.getAcceptedQuantity());
        dto.setRejectedQuantity(slip.getRejectedQuantity());
        dto.setRejectionReason(slip.getRejectionReason());
        dto.setWarehouseId(slip.getWarehouseId());
        dto.setWarehouseName(slip.getWarehouseName());
        dto.setNotes(slip.getNotes());
        dto.setCreatedAt(slip.getCreatedAt());
        dto.setUpdatedAt(slip.getUpdatedAt());

        // Agricultural lot fields
        dto.setLocation(slip.getLocation());
        dto.setDeliveryType(slip.getDeliveryType());
        dto.setTcName(slip.getTcName());
        dto.setFieldProducerId(slip.getFieldProducerId());
        dto.setFieldProducerName(slip.getFieldProducerName());
        dto.setPlantVariantId(slip.getPlantVariantId());
        dto.setPlantVariantName(slip.getPlantVariantName());
        dto.setTemporaryLotNumber(slip.getTemporaryLotNumber());
        dto.setFatherName(slip.getFatherName());
        dto.setVillage(slip.getVillage());
        dto.setInwardGatePassNumber(slip.getInwardGatePassNumber());
        dto.setWeighbridgeQuantity(slip.getWeighbridgeQuantity());
        dto.setGodownName(slip.getGodownName());
        dto.setSupervisorName(slip.getSupervisorName());
        dto.setStackNumber(slip.getStackNumber());
        dto.setHamaliContractor(slip.getHamaliContractor());
        dto.setEmptyGunnyWeightKg(slip.getEmptyGunnyWeightKg());
        dto.setLotNumber(slip.getLotNumber());
        dto.setInputType(slip.getInputType());
        dto.setMoisturePercent(slip.getMoisturePercent());
        dto.setUnloadingSlipNumber(slip.getUnloadingSlipNumber());
        dto.setBagWeightMode(slip.getBagWeightMode());
        dto.setBagType(slip.getBagType());
        dto.setBagSizeKg(slip.getBagSizeKg());
        dto.setQuantityPerBag(slip.getQuantityPerBag());
        dto.setNumberOfBags(slip.getNumberOfBags());

        if (slip.getItems() != null) {
            dto.setItems(slip.getItems().stream().map(item -> {
                IntakeSlipDto.ItemDto idto = new IntakeSlipDto.ItemDto();
                idto.setId(item.getId());
                idto.setProductId(item.getProductId());
                idto.setProductName(item.getProductName());
                idto.setReceivedQuantity(item.getReceivedQuantity());
                idto.setAcceptedQuantity(item.getAcceptedQuantity());
                idto.setRejectedQuantity(item.getRejectedQuantity());
                idto.setUnit(item.getUnit());
                idto.setUnitPrice(item.getUnitPrice());
                idto.setLotNumber(item.getLotNumber());
                idto.setQualityRemarks(item.getQualityRemarks());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private IntakeSlip findOrThrow(UUID id) {
        return intakeSlipRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Intake slip not found: " + id));
    }

    private String generateSlipNumber(UUID tenantId) {
        long count = intakeSlipRepository.countByTenantIdAndDeletedAtIsNull(tenantId);
        String year = String.valueOf(Year.now().getValue());
        return String.format("SLIP-%s-%03d", year, count + 1);
    }
}

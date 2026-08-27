package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.dto.CreateFreightPaymentRequest;
import com.erp.platform.modules.purchase.dto.FreightPaymentDto;
import com.erp.platform.modules.purchase.entity.FreightPayment;
import com.erp.platform.modules.purchase.entity.GoodsReceipt;
import com.erp.platform.modules.purchase.repository.FreightPaymentRepository;
import com.erp.platform.modules.purchase.repository.GoodsReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FreightPaymentService {

    private final FreightPaymentRepository freightPaymentRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final TenantContext tenantContext;

    public PageResponse<FreightPaymentDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(freightPaymentRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public FreightPaymentDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        return toDto(freightPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Freight payment not found: " + id)));
    }

    @Transactional
    public FreightPaymentDto create(CreateFreightPaymentRequest request) {
        UUID tenantId = tenantContext.current();

        FreightPayment fp = new FreightPayment();
        fp.setTenantId(tenantId);
        fp.setPaymentNumber(generateNumber());
        fp.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());

        // Populate from linked GRN if provided
        if (request.getGoodsReceiptId() != null) {
            goodsReceiptRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, request.getGoodsReceiptId())
                    .ifPresent(grn -> {
                        fp.setGoodsReceiptId(grn.getId());
                        fp.setGrnNumber(grn.getGrnNumber());
                        if (request.getCarrierName() == null) fp.setCarrierName(grn.getFreightCarrierName());
                        if (request.getLrNumber() == null) fp.setLrNumber(grn.getLrNumber());
                        if (request.getDcNumber() == null) fp.setDcNumber(grn.getDcNumber());
                        if (request.getVehicleNumber() == null) fp.setVehicleNumber(grn.getVehicleNumber());
                        if (request.getFreightAmount() == null && grn.getFreightAmount() != null)
                            fp.setFreightAmount(grn.getFreightAmount());
                        if (request.getAdvancePaid() == null && grn.getFreightAdvancePaid() != null)
                            fp.setAdvancePaid(grn.getFreightAdvancePaid());
                    });
        }

        // Override with explicit request values
        if (request.getCarrierName() != null) fp.setCarrierName(request.getCarrierName());
        if (request.getLrNumber() != null) fp.setLrNumber(request.getLrNumber());
        if (request.getDcNumber() != null) fp.setDcNumber(request.getDcNumber());
        if (request.getVehicleNumber() != null) fp.setVehicleNumber(request.getVehicleNumber());
        if (request.getFreightAmount() != null) fp.setFreightAmount(request.getFreightAmount());
        if (request.getAdvancePaid() != null) fp.setAdvancePaid(request.getAdvancePaid());

        BigDecimal freightTotal = fp.getFreightAmount() != null ? fp.getFreightAmount() : BigDecimal.ZERO;
        BigDecimal advance = fp.getAdvancePaid() != null ? fp.getAdvancePaid() : BigDecimal.ZERO;
        BigDecimal paid = request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO;
        fp.setAmountPaid(paid);
        fp.setBalanceDue(freightTotal.subtract(advance).subtract(paid).max(BigDecimal.ZERO));

        fp.setPaymentMode(request.getPaymentMode());
        fp.setChequeNumber(request.getChequeNumber());
        fp.setBankName(request.getBankName());
        fp.setReferenceNumber(request.getReferenceNumber());
        fp.setNotes(request.getNotes());
        fp.setStatus(fp.getBalanceDue().compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "DRAFT");

        FreightPayment saved = freightPaymentRepository.save(fp);
        log.info("Freight payment created: id={}, number={}", saved.getId(), saved.getPaymentNumber());
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        FreightPayment fp = freightPaymentRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Freight payment not found: " + id));
        fp.setDeletedAt(LocalDateTime.now());
        freightPaymentRepository.save(fp);
    }

    private FreightPaymentDto toDto(FreightPayment fp) {
        FreightPaymentDto dto = new FreightPaymentDto();
        dto.setId(fp.getId());
        dto.setTenantId(fp.getTenantId());
        dto.setPaymentNumber(fp.getPaymentNumber());
        dto.setGoodsReceiptId(fp.getGoodsReceiptId());
        dto.setGrnNumber(fp.getGrnNumber());
        dto.setCarrierName(fp.getCarrierName());
        dto.setLrNumber(fp.getLrNumber());
        dto.setDcNumber(fp.getDcNumber());
        dto.setVehicleNumber(fp.getVehicleNumber());
        dto.setPaymentDate(fp.getPaymentDate());
        dto.setFreightAmount(fp.getFreightAmount());
        dto.setAdvancePaid(fp.getAdvancePaid());
        dto.setAmountPaid(fp.getAmountPaid());
        dto.setBalanceDue(fp.getBalanceDue());
        dto.setPaymentMode(fp.getPaymentMode());
        dto.setChequeNumber(fp.getChequeNumber());
        dto.setBankName(fp.getBankName());
        dto.setReferenceNumber(fp.getReferenceNumber());
        dto.setStatus(fp.getStatus());
        dto.setNotes(fp.getNotes());
        dto.setCreatedAt(fp.getCreatedAt());
        return dto;
    }

    private String generateNumber() {
        return "FP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateProducerPaymentRequest;
import com.erp.platform.modules.agri.dto.DeductionItemRequest;
import com.erp.platform.modules.agri.dto.ProducerPaymentDto;
import com.erp.platform.modules.agri.entity.FieldProducer;
import com.erp.platform.modules.agri.entity.ProducerContract;
import com.erp.platform.modules.agri.entity.ProducerPayment;
import com.erp.platform.modules.agri.entity.ProducerPaymentDeduction;
import com.erp.platform.modules.agri.repository.FieldProducerRepository;
import com.erp.platform.modules.agri.repository.ProducerContractRepository;
import com.erp.platform.modules.agri.repository.ProducerPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProducerPaymentService {

    private final ProducerPaymentRepository repository;
    private final FieldProducerRepository producerRepository;
    private final ProducerContractRepository contractRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProducerPaymentDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<ProducerPayment> all = repository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<ProducerPaymentDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<ProducerPaymentDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public ProducerPaymentDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProducerPaymentDto create(CreateProducerPaymentRequest req) {
        ProducerPayment entity = new ProducerPayment();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, req);
        entity = repository.save(entity);
        log.info("ProducerPayment created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ProducerPaymentDto update(UUID id, CreateProducerPaymentRequest req) {
        ProducerPayment entity = findOrThrow(id);
        applyRequest(entity, req);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProducerPayment entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    @Transactional
    public ProducerPaymentDto calculateDeductions(UUID paymentId, List<DeductionItemRequest> requests) {
        ProducerPayment payment = findOrThrow(paymentId);
        if ("PAID".equals(payment.getStatus())) {
            throw AppException.badRequest("Cannot recalculate deductions for a PAID payment");
        }
        BigDecimal gross = payment.getGrossAmount() != null ? payment.getGrossAmount() : BigDecimal.ZERO;
        UUID tenantId = tenantContext.current();

        payment.getDeductionItems().clear();
        BigDecimal totalDeduction = BigDecimal.ZERO;

        if (requests != null) {
            for (DeductionItemRequest req : requests) {
                if (req.getDeductionName() == null || req.getDeductionName().isBlank()) continue;
                ProducerPaymentDeduction item = new ProducerPaymentDeduction();
                item.setTenantId(tenantId);
                item.setPaymentId(paymentId);
                item.setPossibleDeductionId(req.getPossibleDeductionId());
                item.setDeductionName(req.getDeductionName());
                item.setDeductionType(req.getDeductionType());
                item.setRate(req.getRate());
                item.setQuantity(req.getQuantity());
                item.setRemarks(req.getRemarks());

                BigDecimal amount = computeAmount(req, gross);
                item.setAmount(amount);
                totalDeduction = totalDeduction.add(amount);
                payment.getDeductionItems().add(item);
            }
        }

        payment.setDeductions(totalDeduction);
        payment.setNetAmount(gross.subtract(totalDeduction).max(BigDecimal.ZERO));
        payment = repository.save(payment);
        log.info("Deductions recalculated for payment {}: total={}", paymentId, totalDeduction);
        return toDto(payment);
    }

    private BigDecimal computeAmount(DeductionItemRequest req, BigDecimal grossAmount) {
        String type = req.getDeductionType() != null ? req.getDeductionType().toUpperCase() : "FIXED";
        BigDecimal rate = req.getRate() != null ? req.getRate() : BigDecimal.ZERO;
        BigDecimal qty  = req.getQuantity() != null ? req.getQuantity() : BigDecimal.ONE;
        return switch (type) {
            case "PERCENTAGE" -> grossAmount.multiply(rate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case "PER_UNIT", "WEIGHT" -> qty.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            default -> req.getAmount() != null
                    ? req.getAmount().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        };
    }

    private void applyRequest(ProducerPayment entity, CreateProducerPaymentRequest req) {
        entity.setPaymentNumber(req.getPaymentNumber());
        entity.setPaymentDate(req.getPaymentDate());
        entity.setDeliveredQuantity(req.getDeliveredQuantity());
        entity.setQualifiedQuantity(req.getQualifiedQuantity());
        entity.setRejectedQuantity(req.getRejectedQuantity());
        entity.setRatePerUnit(req.getRatePerUnit());
        entity.setGrossAmount(req.getGrossAmount());
        entity.setDeductions(req.getDeductions());
        entity.setNetAmount(req.getNetAmount());
        entity.setPaymentMethod(req.getPaymentMethod());
        entity.setReferenceNumber(req.getReferenceNumber());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : "PENDING");
        entity.setRemarks(req.getRemarks());

        UUID tenantId = tenantContext.current();
        if (req.getFieldProducerId() != null) {
            FieldProducer producer = producerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getFieldProducerId()).orElse(null);
            entity.setFieldProducer(producer);
        }
        if (req.getProducerContractId() != null) {
            ProducerContract contract = contractRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getProducerContractId()).orElse(null);
            entity.setProducerContract(contract);
        }
    }

    private ProducerPayment findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Producer payment not found: " + id));
    }

    private ProducerPaymentDto toDto(ProducerPayment e) {
        ProducerPaymentDto dto = new ProducerPaymentDto();
        dto.setId(e.getId());
        dto.setPaymentNumber(e.getPaymentNumber());
        dto.setPaymentDate(e.getPaymentDate());
        dto.setDeliveredQuantity(e.getDeliveredQuantity());
        dto.setQualifiedQuantity(e.getQualifiedQuantity());
        dto.setRejectedQuantity(e.getRejectedQuantity());
        dto.setRatePerUnit(e.getRatePerUnit());
        dto.setGrossAmount(e.getGrossAmount());
        dto.setDeductions(e.getDeductions());
        dto.setNetAmount(e.getNetAmount());
        dto.setPaymentMethod(e.getPaymentMethod());
        dto.setReferenceNumber(e.getReferenceNumber());
        dto.setStatus(e.getStatus());
        dto.setRemarks(e.getRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        if (e.getFieldProducer() != null) {
            dto.setFieldProducerId(e.getFieldProducer().getId());
            dto.setFieldProducerName(e.getFieldProducer().getName());
        }
        if (e.getProducerContract() != null) {
            dto.setProducerContractId(e.getProducerContract().getId());
            dto.setContractNumber(e.getProducerContract().getContractNumber());
        }
        if (e.getDeductionItems() != null) {
            dto.setDeductionItems(e.getDeductionItems().stream().map(d -> {
                ProducerPaymentDto.DeductionItemDto idto = new ProducerPaymentDto.DeductionItemDto();
                idto.setId(d.getId());
                idto.setPossibleDeductionId(d.getPossibleDeductionId());
                idto.setDeductionName(d.getDeductionName());
                idto.setDeductionType(d.getDeductionType());
                idto.setRate(d.getRate());
                idto.setQuantity(d.getQuantity());
                idto.setAmount(d.getAmount());
                idto.setRemarks(d.getRemarks());
                return idto;
            }).toList());
        }
        return dto;
    }
}

package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.dto.CreateProducerFinalPaymentRequest;
import com.erp.platform.modules.purchase.dto.ProducerFinalPaymentDto;
import com.erp.platform.modules.purchase.entity.ProducerFinalPayment;
import com.erp.platform.modules.purchase.repository.ProducerFinalPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProducerFinalPaymentService {

    private final ProducerFinalPaymentRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<ProducerFinalPaymentDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public ProducerFinalPaymentDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProducerFinalPaymentDto create(CreateProducerFinalPaymentRequest request) {
        UUID tenantId = tenantContext.current();
        ProducerFinalPayment entity = new ProducerFinalPayment();
        entity.setTenantId(tenantId);
        entity.setPaymentNumber(request.paymentNumber());
        entity.setPaymentDate(request.paymentDate());
        entity.setProducerName(request.producerName());
        entity.setCropName(request.cropName());
        entity.setSeason(request.season());
        entity.setLotNumber(request.lotNumber());
        entity.setContractQuantityKgs(request.contractQuantityKgs());
        entity.setActualSuppliedKgs(request.actualSuppliedKgs());
        entity.setRatePerKg(request.ratePerKg());
        entity.setBasicAmount(request.basicAmount());
        entity.setDeductions(request.deductions());
        entity.setAdvancePaid(request.advancePaid());
        entity.setNetPayable(request.netPayable());
        entity.setStatus(request.status() != null ? request.status() : "DRAFT");
        entity = repository.save(entity);
        log.info("ProducerFinalPayment created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public ProducerFinalPaymentDto update(UUID id, CreateProducerFinalPaymentRequest request) {
        ProducerFinalPayment entity = findOrThrow(id);
        entity.setPaymentNumber(request.paymentNumber());
        entity.setPaymentDate(request.paymentDate());
        entity.setProducerName(request.producerName());
        entity.setCropName(request.cropName());
        entity.setSeason(request.season());
        entity.setLotNumber(request.lotNumber());
        entity.setContractQuantityKgs(request.contractQuantityKgs());
        entity.setActualSuppliedKgs(request.actualSuppliedKgs());
        entity.setRatePerKg(request.ratePerKg());
        entity.setBasicAmount(request.basicAmount());
        entity.setDeductions(request.deductions());
        entity.setAdvancePaid(request.advancePaid());
        entity.setNetPayable(request.netPayable());
        entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProducerFinalPayment entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private ProducerFinalPayment findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("ProducerFinalPayment not found: " + id));
    }

    private ProducerFinalPaymentDto toDto(ProducerFinalPayment e) {
        return new ProducerFinalPaymentDto(
                e.getId(),
                e.getPaymentNumber(),
                e.getPaymentDate(),
                e.getProducerName(),
                e.getCropName(),
                e.getSeason(),
                e.getLotNumber(),
                e.getContractQuantityKgs(),
                e.getActualSuppliedKgs(),
                e.getRatePerKg(),
                e.getBasicAmount(),
                e.getDeductions(),
                e.getAdvancePaid(),
                e.getNetPayable(),
                e.getStatus()
        );
    }
}

package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateLogisticsPaymentRequest;
import com.erp.platform.modules.agri.dto.LogisticsPaymentDto;
import com.erp.platform.modules.agri.entity.FieldProducer;
import com.erp.platform.modules.agri.entity.LogisticsPayment;
import com.erp.platform.modules.agri.entity.ProducerContract;
import com.erp.platform.modules.agri.repository.FieldProducerRepository;
import com.erp.platform.modules.agri.repository.LogisticsPaymentRepository;
import com.erp.platform.modules.agri.repository.ProducerContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LogisticsPaymentService {

    private final LogisticsPaymentRepository repository;
    private final FieldProducerRepository producerRepository;
    private final ProducerContractRepository contractRepository;
    private final TenantContext tenantContext;

    public PageResponse<LogisticsPaymentDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<LogisticsPayment> all = repository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<LogisticsPaymentDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<LogisticsPaymentDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public LogisticsPaymentDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public LogisticsPaymentDto create(CreateLogisticsPaymentRequest req) {
        LogisticsPayment entity = new LogisticsPayment();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, req);
        entity = repository.save(entity);
        log.info("LogisticsPayment created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public LogisticsPaymentDto update(UUID id, CreateLogisticsPaymentRequest req) {
        LogisticsPayment entity = findOrThrow(id);
        applyRequest(entity, req);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        LogisticsPayment entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(LogisticsPayment entity, CreateLogisticsPaymentRequest req) {
        entity.setPaymentNumber(req.getPaymentNumber());
        entity.setPaymentDate(req.getPaymentDate());
        entity.setLogisticsProvider(req.getLogisticsProvider());
        entity.setVehicleNumber(req.getVehicleNumber());
        entity.setQuantityHandled(req.getQuantityHandled());
        entity.setHandlingUom(req.getHandlingUom());
        entity.setRatePerUnit(req.getRatePerUnit());
        entity.setTotalAmount(req.getTotalAmount());
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

    private LogisticsPayment findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Logistics payment not found: " + id));
    }

    private LogisticsPaymentDto toDto(LogisticsPayment e) {
        LogisticsPaymentDto dto = new LogisticsPaymentDto();
        dto.setId(e.getId());
        dto.setPaymentNumber(e.getPaymentNumber());
        dto.setPaymentDate(e.getPaymentDate());
        dto.setLogisticsProvider(e.getLogisticsProvider());
        dto.setVehicleNumber(e.getVehicleNumber());
        dto.setQuantityHandled(e.getQuantityHandled());
        dto.setHandlingUom(e.getHandlingUom());
        dto.setRatePerUnit(e.getRatePerUnit());
        dto.setTotalAmount(e.getTotalAmount());
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
        return dto;
    }
}

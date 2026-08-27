package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateProducerAdvanceRequest;
import com.erp.platform.modules.agri.dto.ProducerAdvanceDto;
import com.erp.platform.modules.agri.entity.ProducerAdvance;
import com.erp.platform.modules.agri.repository.FieldProducerRepository;
import com.erp.platform.modules.agri.repository.ProducerAdvanceRepository;
import com.erp.platform.modules.agri.repository.ProducerContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProducerAdvanceService {

    private final ProducerAdvanceRepository repository;
    private final FieldProducerRepository producerRepository;
    private final ProducerContractRepository contractRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProducerAdvanceDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<ProducerAdvance> all = repository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<ProducerAdvanceDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<ProducerAdvanceDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public ProducerAdvanceDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProducerAdvanceDto create(CreateProducerAdvanceRequest req) {
        UUID tenantId = tenantContext.current();
        ProducerAdvance entity = new ProducerAdvance();
        entity.setTenantId(tenantId);
        entity.setAdvanceNumber(req.getAdvanceNumber());
        entity.setAdvanceDate(req.getAdvanceDate());
        entity.setAdvanceAmount(req.getAdvanceAmount());
        entity.setAdjustedAmount(BigDecimal.ZERO);
        entity.setPaymentMethod(req.getPaymentMethod());
        entity.setReferenceNumber(req.getReferenceNumber());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : "PENDING");
        entity.setRemarks(req.getRemarks());
        if (req.getFieldProducerId() != null) {
            producerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getFieldProducerId())
                    .ifPresent(entity::setFieldProducer);
        }
        if (req.getProducerContractId() != null) {
            contractRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getProducerContractId())
                    .ifPresent(entity::setProducerContract);
        }
        return toDto(repository.save(entity));
    }

    @Transactional
    public ProducerAdvanceDto update(UUID id, CreateProducerAdvanceRequest req) {
        UUID tenantId = tenantContext.current();
        ProducerAdvance entity = findOrThrow(id);
        entity.setAdvanceNumber(req.getAdvanceNumber());
        entity.setAdvanceDate(req.getAdvanceDate());
        entity.setAdvanceAmount(req.getAdvanceAmount());
        entity.setPaymentMethod(req.getPaymentMethod());
        entity.setReferenceNumber(req.getReferenceNumber());
        if (req.getStatus() != null) entity.setStatus(req.getStatus());
        entity.setRemarks(req.getRemarks());
        if (req.getFieldProducerId() != null) {
            producerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getFieldProducerId())
                    .ifPresent(entity::setFieldProducer);
        }
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProducerAdvance entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private ProducerAdvance findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Producer advance not found: " + id));
    }

    private ProducerAdvanceDto toDto(ProducerAdvance e) {
        ProducerAdvanceDto dto = new ProducerAdvanceDto();
        dto.setId(e.getId());
        dto.setAdvanceNumber(e.getAdvanceNumber());
        dto.setAdvanceDate(e.getAdvanceDate());
        dto.setAdvanceAmount(e.getAdvanceAmount());
        dto.setAdjustedAmount(e.getAdjustedAmount());
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

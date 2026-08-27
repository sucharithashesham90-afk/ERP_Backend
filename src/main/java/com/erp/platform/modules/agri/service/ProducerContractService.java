package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateProducerContractRequest;
import com.erp.platform.modules.agri.dto.ProducerContractDto;
import com.erp.platform.modules.agri.entity.FieldProducer;
import com.erp.platform.modules.agri.entity.PlantVariant;
import com.erp.platform.modules.agri.entity.ProducerContract;
import com.erp.platform.modules.agri.repository.FieldProducerRepository;
import com.erp.platform.modules.agri.repository.PlantVariantRepository;
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
public class ProducerContractService {

    private final ProducerContractRepository repository;
    private final FieldProducerRepository producerRepository;
    private final PlantVariantRepository variantRepository;
    private final TenantContext tenantContext;

    public PageResponse<ProducerContractDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<ProducerContract> all = repository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<ProducerContractDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<ProducerContractDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public ProducerContractDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ProducerContractDto create(CreateProducerContractRequest req) {
        UUID tenantId = tenantContext.current();
        ProducerContract entity = new ProducerContract();
        entity.setTenantId(tenantId);
        applyRequest(entity, req, tenantId);
        entity = repository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public ProducerContractDto update(UUID id, CreateProducerContractRequest req) {
        UUID tenantId = tenantContext.current();
        ProducerContract entity = findOrThrow(id);
        applyRequest(entity, req, tenantId);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        ProducerContract entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(ProducerContract e, CreateProducerContractRequest req, UUID tenantId) {
        e.setContractNumber(req.getContractNumber());
        e.setContractDate(req.getContractDate());
        e.setExpectedHarvestDate(req.getExpectedHarvestDate());
        e.setContractedArea(req.getContractedArea());
        e.setExpectedQuantity(req.getExpectedQuantity());
        e.setAgreedRate(req.getAgreedRate());
        e.setStatus(req.getStatus());
        e.setSeason(req.getSeason());
        e.setTermsAndConditions(req.getTermsAndConditions());
        e.setRemarks(req.getRemarks());
        if (req.getFieldProducerId() != null) {
            producerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getFieldProducerId())
                    .ifPresent(e::setFieldProducer);
        }
        if (req.getPlantVariantId() != null) {
            variantRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, req.getPlantVariantId())
                    .ifPresent(e::setPlantVariant);
        }
    }

    private ProducerContract findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Producer contract not found: " + id));
    }

    private ProducerContractDto toDto(ProducerContract e) {
        ProducerContractDto dto = new ProducerContractDto();
        dto.setId(e.getId());
        dto.setContractNumber(e.getContractNumber());
        dto.setContractDate(e.getContractDate());
        dto.setExpectedHarvestDate(e.getExpectedHarvestDate());
        dto.setContractedArea(e.getContractedArea());
        dto.setExpectedQuantity(e.getExpectedQuantity());
        dto.setAgreedRate(e.getAgreedRate());
        dto.setStatus(e.getStatus());
        dto.setSeason(e.getSeason());
        dto.setTermsAndConditions(e.getTermsAndConditions());
        dto.setRemarks(e.getRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        if (e.getFieldProducer() != null) {
            dto.setFieldProducerId(e.getFieldProducer().getId());
            dto.setFieldProducerName(e.getFieldProducer().getName());
        }
        if (e.getPlantVariant() != null) {
            dto.setPlantVariantId(e.getPlantVariant().getId());
            dto.setPlantVariantName(e.getPlantVariant().getName());
        }
        return dto;
    }
}

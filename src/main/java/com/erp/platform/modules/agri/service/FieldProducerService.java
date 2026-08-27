package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateFieldProducerRequest;
import com.erp.platform.modules.agri.dto.FieldProducerDto;
import com.erp.platform.modules.agri.entity.FieldProducer;
import com.erp.platform.modules.agri.repository.FieldProducerRepository;
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
public class FieldProducerService {

    private final FieldProducerRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<FieldProducerDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<FieldProducer> all = repository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        List<FieldProducerDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<FieldProducerDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public FieldProducerDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public FieldProducerDto create(CreateFieldProducerRequest req) {
        FieldProducer entity = new FieldProducer();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, req);
        entity = repository.save(entity);
        log.info("FieldProducer created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public FieldProducerDto update(UUID id, CreateFieldProducerRequest req) {
        FieldProducer entity = findOrThrow(id);
        applyRequest(entity, req);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        FieldProducer entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(FieldProducer entity, CreateFieldProducerRequest req) {
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setFatherName(req.getFatherName());
        entity.setContactPerson(req.getContactPerson());
        entity.setPhone(req.getPhone());
        entity.setMobile(req.getMobile());
        entity.setAddress(req.getAddress());
        entity.setVillage(req.getVillage());
        entity.setVillageId(req.getVillageId());
        entity.setVillageName(req.getVillageName());
        entity.setDistrict(req.getDistrict());
        entity.setState(req.getState());
        entity.setCountry(req.getCountry());
        entity.setPostalCode(req.getPostalCode());
        entity.setLandHolding(req.getLandHolding());
        entity.setAccountCode(req.getAccountCode());
        entity.setPanNumber(req.getPanNumber());
        entity.setAdharNo(req.getAdharNo());
        entity.setFolioNo(req.getFolioNo());
        entity.setShareholder(req.isShareholder());  // field=shareholder → isShareholder() getter
        entity.setOrganizer(req.isOrganizer());       // field=organizer → isOrganizer() getter
        entity.setBankAccount(req.getBankAccount());
        entity.setBankIfscCode(req.getBankIfscCode());
        entity.setBankName(req.getBankName());
        entity.setBankBranch(req.getBankBranch());
        entity.setActive(req.isActive());
    }

    private FieldProducer findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Field producer not found: " + id));
    }

    private FieldProducerDto toDto(FieldProducer e) {
        FieldProducerDto dto = new FieldProducerDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCode(e.getCode());
        dto.setFatherName(e.getFatherName());
        dto.setContactPerson(e.getContactPerson());
        dto.setPhone(e.getPhone());
        dto.setMobile(e.getMobile());
        dto.setAddress(e.getAddress());
        dto.setVillage(e.getVillage());
        dto.setVillageId(e.getVillageId());
        dto.setVillageName(e.getVillageName());
        dto.setDistrict(e.getDistrict());
        dto.setState(e.getState());
        dto.setCountry(e.getCountry());
        dto.setPostalCode(e.getPostalCode());
        dto.setLandHolding(e.getLandHolding());
        dto.setAccountCode(e.getAccountCode());
        dto.setOutstandingBalance(e.getOutstandingBalance());
        dto.setPanNumber(e.getPanNumber());
        dto.setAdharNo(e.getAdharNo());
        dto.setFolioNo(e.getFolioNo());
        dto.setShareholder(e.isShareholder());  // field=shareholder → isShareholder() getter
        dto.setOrganizer(e.isOrganizer());       // field=organizer → isOrganizer() getter
        dto.setBankAccount(e.getBankAccount());
        dto.setBankIfscCode(e.getBankIfscCode());
        dto.setBankName(e.getBankName());
        dto.setBankBranch(e.getBankBranch());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}

package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateNurseryRequest;
import com.erp.platform.modules.agri.dto.NurseryDto;
import com.erp.platform.modules.agri.entity.Nursery;
import com.erp.platform.modules.agri.repository.NurseryRepository;
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
public class NurseryService {

    private final NurseryRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<NurseryDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public NurseryDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public NurseryDto create(CreateNurseryRequest request) {
        UUID tenantId = tenantContext.current();
        Nursery entity = new Nursery();
        entity.setTenantId(tenantId);
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setContactPerson(request.contactPerson());
        entity.setPhone(request.phone());
        entity.setAddress(request.address());
        entity.setVillage(request.village());
        entity.setDistrict(request.district());
        entity.setState(request.state());
        entity.setCapacityKgs(request.capacityKgs());
        entity.setActive(request.active());
        entity = repository.save(entity);
        log.info("Nursery created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public NurseryDto update(UUID id, CreateNurseryRequest request) {
        Nursery entity = findOrThrow(id);
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setContactPerson(request.contactPerson());
        entity.setPhone(request.phone());
        entity.setAddress(request.address());
        entity.setVillage(request.village());
        entity.setDistrict(request.district());
        entity.setState(request.state());
        entity.setCapacityKgs(request.capacityKgs());
        entity.setActive(request.active());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        Nursery entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private Nursery findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Nursery not found: " + id));
    }

    private NurseryDto toDto(Nursery e) {
        return new NurseryDto(
                e.getId(),
                e.getCode(),
                e.getName(),
                e.getContactPerson(),
                e.getPhone(),
                e.getAddress(),
                e.getVillage(),
                e.getDistrict(),
                e.getState(),
                e.getCapacityKgs(),
                e.isActive()
        );
    }
}

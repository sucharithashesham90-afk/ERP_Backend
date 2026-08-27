package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreateDealerRequest;
import com.erp.platform.modules.agri.dto.DealerDto;
import com.erp.platform.modules.agri.entity.Dealer;
import com.erp.platform.modules.agri.entity.DealerRegion;
import com.erp.platform.modules.agri.repository.DealerRegionRepository;
import com.erp.platform.modules.agri.repository.DealerRepository;
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
public class DealerService {

    private final DealerRepository repository;
    private final DealerRegionRepository regionRepository;
    private final TenantContext tenantContext;

    public PageResponse<DealerDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<Dealer> all = repository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        List<DealerDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<DealerDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public DealerDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public DealerDto create(CreateDealerRequest req) {
        Dealer entity = new Dealer();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, req);
        entity = repository.save(entity);
        log.info("Dealer created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public DealerDto update(UUID id, CreateDealerRequest req) {
        Dealer entity = findOrThrow(id);
        applyRequest(entity, req);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        Dealer entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(Dealer entity, CreateDealerRequest req) {
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setContactPerson(req.getContactPerson());
        entity.setPhone(req.getPhone());
        entity.setMobile(req.getMobile());
        entity.setEmail(req.getEmail());
        entity.setAddress(req.getAddress());
        entity.setCity(req.getCity());
        entity.setState(req.getState());
        entity.setPostalCode(req.getPostalCode());
        entity.setDealerType(req.getDealerType());
        entity.setActive(req.isActive());
        entity.setAccountCode(req.getAccountCode());
        entity.setLicenseNumber(req.getLicenseNumber());
        entity.setPanNumber(req.getPanNumber());
        entity.setGstNumber(req.getGstNumber());
        entity.setBankAccount(req.getBankAccount());
        entity.setBankIfscCode(req.getBankIfscCode());
        entity.setBankName(req.getBankName());
        if (req.getCreditLimit() != null) entity.setCreditLimit(req.getCreditLimit());
        if (req.getDealerRegionId() != null) {
            DealerRegion region = regionRepository.findByTenantIdAndIdAndDeletedAtIsNull(
                    tenantContext.current(), req.getDealerRegionId()).orElse(null);
            entity.setDealerRegion(region);
        } else {
            entity.setDealerRegion(null);
        }
    }

    private Dealer findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Dealer not found: " + id));
    }

    private DealerDto toDto(Dealer e) {
        DealerDto dto = new DealerDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCode(e.getCode());
        dto.setContactPerson(e.getContactPerson());
        dto.setPhone(e.getPhone());
        dto.setMobile(e.getMobile());
        dto.setEmail(e.getEmail());
        dto.setAddress(e.getAddress());
        dto.setCity(e.getCity());
        dto.setState(e.getState());
        dto.setPostalCode(e.getPostalCode());
        dto.setDealerType(e.getDealerType());
        dto.setCreditLimit(e.getCreditLimit());
        dto.setOutstandingBalance(e.getOutstandingBalance());
        dto.setAccountCode(e.getAccountCode());
        dto.setLicenseNumber(e.getLicenseNumber());
        dto.setPanNumber(e.getPanNumber());
        dto.setGstNumber(e.getGstNumber());
        dto.setBankAccount(e.getBankAccount());
        dto.setBankIfscCode(e.getBankIfscCode());
        dto.setBankName(e.getBankName());
        dto.setActive(e.isActive());
        dto.setCreatedAt(e.getCreatedAt());
        if (e.getDealerRegion() != null) {
            dto.setDealerRegionId(e.getDealerRegion().getId());
            dto.setDealerRegionName(e.getDealerRegion().getName());
        }
        return dto;
    }
}

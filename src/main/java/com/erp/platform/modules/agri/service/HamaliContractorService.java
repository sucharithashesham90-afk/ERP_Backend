package com.erp.platform.modules.agri.service;
import java.util.UUID;

import com.erp.platform.modules.agri.dto.CreateHamaliContractorRequest;
import com.erp.platform.modules.agri.dto.HamaliContractorDto;
import com.erp.platform.modules.agri.entity.HamaliContractor;
import com.erp.platform.modules.agri.repository.HamaliContractorRepository;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HamaliContractorService {

    private final HamaliContractorRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<HamaliContractorDto> list(int page, int size) {
        UUID tid = tenantContext.current();
        Page<HamaliContractor> p = repository.findByTenantIdAndDeletedAtIsNull(
                tid, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.map(this::toDto));
    }

    public HamaliContractorDto create(CreateHamaliContractorRequest req) {
        HamaliContractor e = new HamaliContractor();
        e.setTenantId(tenantContext.current());
        e.setContractorCode(req.contractorCode());
        e.setContractorName(req.contractorName());
        e.setContractorType(req.contractorType());
        e.setPhoneNumber(req.phoneNumber());
        e.setAddress(req.address());
        e.setStateId(req.stateId());
        e.setStateName(req.stateName());
        e.setDistrictId(req.districtId());
        e.setDistrictName(req.districtName());
        e.setMandalId(req.mandalId());
        e.setMandalName(req.mandalName());
        e.setZipCode(req.zipCode());
        e.setRatePerBag(req.ratePerBag());
        e.setRatePerKg(req.ratePerKg());
        e.setSeason(req.season());
        e.setLocation(req.location());
        e.setActive(req.active() != null ? req.active() : true);
        return toDto(repository.save(e));
    }

    public HamaliContractorDto update(UUID id, CreateHamaliContractorRequest req) {
        HamaliContractor e = repository.findById(id).orElseThrow();
        e.setContractorCode(req.contractorCode());
        e.setContractorName(req.contractorName());
        e.setContractorType(req.contractorType());
        e.setPhoneNumber(req.phoneNumber());
        e.setAddress(req.address());
        e.setStateId(req.stateId());
        e.setStateName(req.stateName());
        e.setDistrictId(req.districtId());
        e.setDistrictName(req.districtName());
        e.setMandalId(req.mandalId());
        e.setMandalName(req.mandalName());
        e.setZipCode(req.zipCode());
        e.setRatePerBag(req.ratePerBag());
        e.setRatePerKg(req.ratePerKg());
        e.setSeason(req.season());
        e.setLocation(req.location());
        e.setActive(req.active());
        return toDto(repository.save(e));
    }

    public void delete(UUID id) {
        HamaliContractor e = repository.findById(id).orElseThrow();
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private HamaliContractorDto toDto(HamaliContractor e) {
        return new HamaliContractorDto(
                e.getId(), e.getContractorCode(), e.getContractorName(),
                e.getContractorType(), e.getPhoneNumber(), e.getAddress(),
                e.getStateId(), e.getStateName(),
                e.getDistrictId(), e.getDistrictName(),
                e.getMandalId(), e.getMandalName(), e.getZipCode(),
                e.getRatePerBag(), e.getRatePerKg(), e.getSeason(),
                e.getLocation(), e.getActive());
    }
}


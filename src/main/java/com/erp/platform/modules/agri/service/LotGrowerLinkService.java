package com.erp.platform.modules.agri.service;
import java.util.UUID;

import com.erp.platform.modules.agri.dto.CreateLotGrowerLinkRequest;
import com.erp.platform.modules.agri.dto.LotGrowerLinkDto;
import com.erp.platform.modules.agri.entity.LotGrowerLink;
import com.erp.platform.modules.agri.repository.LotGrowerLinkRepository;
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
public class LotGrowerLinkService {

    private final LotGrowerLinkRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<LotGrowerLinkDto> list(int page, int size) {
        UUID tid = tenantContext.current();
        Page<LotGrowerLink> p = repository.findByTenantIdAndDeletedAtIsNull(
                tid, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.map(this::toDto));
    }

    public LotGrowerLinkDto create(CreateLotGrowerLinkRequest req) {
        LotGrowerLink e = new LotGrowerLink();
        e.setTenantId(tenantContext.current());
        e.setLinkNumber(req.linkNumber());
        e.setLotNumber(req.lotNumber());
        e.setGrowerCode(req.growerCode());
        e.setGrowerName(req.growerName());
        e.setVillage(req.village());
        e.setOrganizer(req.organizer());
        e.setContractedQtyKgs(req.contractedQtyKgs());
        e.setSuppliedQtyKgs(req.suppliedQtyKgs());
        e.setSeason(req.season());
        e.setCropName(req.cropName());
        e.setRemarks(req.remarks());
        return toDto(repository.save(e));
    }

    public LotGrowerLinkDto update(UUID id, CreateLotGrowerLinkRequest req) {
        LotGrowerLink e = repository.findById(id).orElseThrow();
        e.setLinkNumber(req.linkNumber());
        e.setLotNumber(req.lotNumber());
        e.setGrowerCode(req.growerCode());
        e.setGrowerName(req.growerName());
        e.setVillage(req.village());
        e.setOrganizer(req.organizer());
        e.setContractedQtyKgs(req.contractedQtyKgs());
        e.setSuppliedQtyKgs(req.suppliedQtyKgs());
        e.setSeason(req.season());
        e.setCropName(req.cropName());
        e.setRemarks(req.remarks());
        return toDto(repository.save(e));
    }

    public void delete(UUID id) {
        LotGrowerLink e = repository.findById(id).orElseThrow();
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private LotGrowerLinkDto toDto(LotGrowerLink e) {
        return new LotGrowerLinkDto(
                e.getId(), e.getLinkNumber(), e.getLotNumber(), e.getGrowerCode(),
                e.getGrowerName(), e.getVillage(), e.getOrganizer(),
                e.getContractedQtyKgs(), e.getSuppliedQtyKgs(),
                e.getSeason(), e.getCropName(), e.getRemarks());
    }
}


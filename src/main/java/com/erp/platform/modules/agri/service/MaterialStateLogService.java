package com.erp.platform.modules.agri.service;
import java.util.UUID;

import com.erp.platform.modules.agri.dto.CreateMaterialStateLogRequest;
import com.erp.platform.modules.agri.dto.MaterialStateLogDto;
import com.erp.platform.modules.agri.entity.MaterialStateLog;
import com.erp.platform.modules.agri.repository.MaterialStateLogRepository;
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
public class MaterialStateLogService {

    private final MaterialStateLogRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<MaterialStateLogDto> list(int page, int size) {
        UUID tid = tenantContext.current();
        Page<MaterialStateLog> p = repository.findByTenantIdAndDeletedAtIsNull(
                tid, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.map(this::toDto));
    }

    public MaterialStateLogDto create(CreateMaterialStateLogRequest req) {
        MaterialStateLog e = new MaterialStateLog();
        e.setTenantId(tenantContext.current());
        e.setLogNumber(req.logNumber());
        e.setLogDate(req.logDate());
        e.setLotNumber(req.lotNumber());
        e.setFromState(req.fromState());
        e.setToState(req.toState());
        e.setProcessType(req.processType());
        e.setQuantityKgs(req.quantityKgs());
        e.setLocation(req.location());
        e.setOperatorName(req.operatorName());
        e.setRemarks(req.remarks());
        return toDto(repository.save(e));
    }

    public MaterialStateLogDto update(UUID id, CreateMaterialStateLogRequest req) {
        MaterialStateLog e = repository.findById(id).orElseThrow();
        e.setLogNumber(req.logNumber());
        e.setLogDate(req.logDate());
        e.setLotNumber(req.lotNumber());
        e.setFromState(req.fromState());
        e.setToState(req.toState());
        e.setProcessType(req.processType());
        e.setQuantityKgs(req.quantityKgs());
        e.setLocation(req.location());
        e.setOperatorName(req.operatorName());
        e.setRemarks(req.remarks());
        return toDto(repository.save(e));
    }

    public void delete(UUID id) {
        MaterialStateLog e = repository.findById(id).orElseThrow();
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private MaterialStateLogDto toDto(MaterialStateLog e) {
        return new MaterialStateLogDto(
                e.getId(), e.getLogNumber(), e.getLogDate(), e.getLotNumber(),
                e.getFromState(), e.getToState(), e.getProcessType(),
                e.getQuantityKgs(), e.getLocation(), e.getOperatorName(),
                e.getRemarks());
    }
}


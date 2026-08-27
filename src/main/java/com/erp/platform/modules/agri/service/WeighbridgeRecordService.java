package com.erp.platform.modules.agri.service;
import java.util.UUID;

import com.erp.platform.modules.agri.dto.CreateWeighbridgeRecordRequest;
import com.erp.platform.modules.agri.dto.WeighbridgeRecordDto;
import com.erp.platform.modules.agri.entity.WeighbridgeRecord;
import com.erp.platform.modules.agri.repository.WeighbridgeRecordRepository;
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
public class WeighbridgeRecordService {

    private final WeighbridgeRecordRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<WeighbridgeRecordDto> list(int page, int size) {
        UUID tid = tenantContext.current();
        Page<WeighbridgeRecord> p = repository.findByTenantIdAndDeletedAtIsNull(
                tid, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.map(this::toDto));
    }

    public WeighbridgeRecordDto create(CreateWeighbridgeRecordRequest req) {
        WeighbridgeRecord e = new WeighbridgeRecord();
        e.setTenantId(tenantContext.current());
        e.setSlipNumber(req.slipNumber());
        e.setWeighDate(req.weighDate());
        e.setVehicleNumber(req.vehicleNumber());
        e.setGrossWeight(req.grossWeight());
        e.setTareWeight(req.tareWeight());
        e.setNetWeight(req.netWeight());
        e.setLocation(req.location());
        e.setLinkedIntakeSlip(req.linkedIntakeSlip());
        e.setDriverName(req.driverName());
        e.setRemarks(req.remarks());
        return toDto(repository.save(e));
    }

    public WeighbridgeRecordDto update(UUID id, CreateWeighbridgeRecordRequest req) {
        WeighbridgeRecord e = repository.findById(id).orElseThrow();
        e.setSlipNumber(req.slipNumber());
        e.setWeighDate(req.weighDate());
        e.setVehicleNumber(req.vehicleNumber());
        e.setGrossWeight(req.grossWeight());
        e.setTareWeight(req.tareWeight());
        e.setNetWeight(req.netWeight());
        e.setLocation(req.location());
        e.setLinkedIntakeSlip(req.linkedIntakeSlip());
        e.setDriverName(req.driverName());
        e.setRemarks(req.remarks());
        return toDto(repository.save(e));
    }

    public void delete(UUID id) {
        WeighbridgeRecord e = repository.findById(id).orElseThrow();
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private WeighbridgeRecordDto toDto(WeighbridgeRecord e) {
        return new WeighbridgeRecordDto(
                e.getId(), e.getSlipNumber(), e.getWeighDate(),
                e.getVehicleNumber(), e.getGrossWeight(), e.getTareWeight(),
                e.getNetWeight(), e.getLocation(), e.getLinkedIntakeSlip(),
                e.getDriverName(), e.getRemarks());
    }
}


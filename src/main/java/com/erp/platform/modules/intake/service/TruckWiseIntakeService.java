package com.erp.platform.modules.intake.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.intake.dto.CreateTruckWiseIntakeRequest;
import com.erp.platform.modules.intake.dto.TruckWiseIntakeDto;
import com.erp.platform.modules.intake.entity.TruckWiseIntake;
import com.erp.platform.modules.intake.repository.TruckWiseIntakeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class TruckWiseIntakeService {

    private final TruckWiseIntakeRepository repository;
    private final TenantContext tenantContext;
    private final ObjectMapper objectMapper;

    public PageResponse<TruckWiseIntakeDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    public TruckWiseIntakeDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public TruckWiseIntakeDto create(CreateTruckWiseIntakeRequest request) {
        UUID tenantId = tenantContext.current();
        TruckWiseIntake entity = new TruckWiseIntake();
        entity.setTenantId(tenantId);
        entity.setIntakeSlipNumber(request.intakeSlipNumber());
        entity.setIntakeDate(request.intakeDate());
        entity.setLocation(request.location());
        entity.setDeliveryType(request.deliveryType());
        entity.setVehicleNumber(request.vehicleNumber());
        entity.setInwardGatePassNumber(request.inwardGatePassNumber());
        entity.setLrNumber(request.lrNumber());
        entity.setTransport(request.transport());
        entity.setTotalFreight(request.totalFreight());
        entity.setWeighBridgeQty(request.weighBridgeQty());
        entity.setStnNumber(request.stnNumber());
        entity.setStnDate(request.stnDate());
        entity.setStatus(request.status() != null ? request.status() : "DRAFT");
        entity = repository.save(entity);
        log.info("TruckWiseIntake created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public TruckWiseIntakeDto update(UUID id, CreateTruckWiseIntakeRequest request) {
        TruckWiseIntake entity = findOrThrow(id);
        entity.setIntakeSlipNumber(request.intakeSlipNumber());
        entity.setIntakeDate(request.intakeDate());
        entity.setLocation(request.location());
        entity.setDeliveryType(request.deliveryType());
        entity.setVehicleNumber(request.vehicleNumber());
        entity.setInwardGatePassNumber(request.inwardGatePassNumber());
        entity.setLrNumber(request.lrNumber());
        entity.setTransport(request.transport());
        entity.setTotalFreight(request.totalFreight());
        entity.setWeighBridgeQty(request.weighBridgeQty());
        entity.setStnNumber(request.stnNumber());
        entity.setStnDate(request.stnDate());
        if (request.status() != null) entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        TruckWiseIntake entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    /** Persist the intake's line items (crops/varieties/bags) and mark it completed. */
    @Transactional
    public TruckWiseIntakeDto saveLines(UUID id, Object lines) {
        TruckWiseIntake entity = findOrThrow(id);
        try {
            entity.setLinesJson(objectMapper.writeValueAsString(lines == null ? java.util.List.of() : lines));
        } catch (Exception ex) {
            throw AppException.badRequest("Invalid intake line items: " + ex.getMessage());
        }
        entity.setStatus("COMPLETED");
        log.info("TruckWiseIntake lines saved: {}", id);
        return toDto(repository.save(entity));
    }

    private TruckWiseIntake findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("TruckWiseIntake not found: " + id));
    }

    private TruckWiseIntakeDto toDto(TruckWiseIntake e) {
        return new TruckWiseIntakeDto(
                e.getId(),
                e.getIntakeSlipNumber(),
                e.getIntakeDate(),
                e.getLocation(),
                e.getDeliveryType(),
                e.getVehicleNumber(),
                e.getInwardGatePassNumber(),
                e.getLrNumber(),
                e.getTransport(),
                e.getTotalFreight(),
                e.getWeighBridgeQty(),
                e.getStnNumber(),
                e.getStnDate(),
                e.getStatus()
        );
    }
}

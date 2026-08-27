package com.erp.platform.modules.intake.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.tenant.UserContext;
import com.erp.platform.modules.intake.dto.CreateIntakeScheduleRequest;
import com.erp.platform.modules.intake.dto.IntakeScheduleDto;
import com.erp.platform.modules.intake.entity.IntakeSchedule;
import com.erp.platform.modules.intake.entity.IntakeSchedule.ScheduleStatus;
import com.erp.platform.modules.intake.entity.IntakeScheduleItem;
import com.erp.platform.modules.intake.repository.IntakeScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IntakeScheduleService {

    private final IntakeScheduleRepository intakeScheduleRepository;
    private final TenantContext tenantContext;
    private final UserContext userContext;

    public PageResponse<IntakeScheduleDto> list(ScheduleStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<String> locs = userContext.getAllowedLocations();
        var page = userContext.hasLocationRestriction()
                ? (status != null
                        ? intakeScheduleRepository.findByTenantIdAndStatusAndLocationsAndDeletedAtIsNull(tenantId, status, locs, pageable)
                        : intakeScheduleRepository.findByTenantIdAndLocationsAndDeletedAtIsNull(tenantId, locs, pageable))
                : (status != null
                        ? intakeScheduleRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                        : intakeScheduleRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
        return PageResponse.of(page.map(this::toDto));
    }

    public IntakeScheduleDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public IntakeScheduleDto create(CreateIntakeScheduleRequest request) {
        UUID tenantId = tenantContext.current();

        IntakeSchedule schedule = new IntakeSchedule();
        schedule.setTenantId(tenantId);
        schedule.setScheduleNumber(generateScheduleNumber(tenantId));
        schedule.setVendorId(request.getVendorId());
        schedule.setVendorName(request.getVendorName());
        schedule.setScheduledDate(request.getScheduledDate());
        schedule.setStatus(ScheduleStatus.DRAFT);
        schedule.setVehicleNumber(request.getVehicleNumber());
        schedule.setDriverName(request.getDriverName());
        schedule.setDriverPhone(request.getDriverPhone());
        schedule.setActualQuantity(BigDecimal.ZERO);
        schedule.setWarehouseId(request.getWarehouseId());
        schedule.setWarehouseName(request.getWarehouseName());
        schedule.setNotes(request.getNotes());

        List<IntakeScheduleItem> items = buildItems(schedule, tenantId, request.getItems());
        schedule.setItems(items);

        BigDecimal totalExpected = items.stream()
                .map(IntakeScheduleItem::getExpectedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        schedule.setExpectedQuantity(
                request.getExpectedQuantity() != null ? request.getExpectedQuantity() : totalExpected);

        schedule = intakeScheduleRepository.save(schedule);
        log.info("IntakeSchedule created: id={}, number={}", schedule.getId(), schedule.getScheduleNumber());
        return toDto(schedule);
    }

    @Transactional
    public IntakeScheduleDto update(UUID id, CreateIntakeScheduleRequest request) {
        UUID tenantId = tenantContext.current();
        IntakeSchedule schedule = findOrThrow(id);
        if (schedule.getStatus() != ScheduleStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT schedules can be edited");
        }
        schedule.setVendorId(request.getVendorId());
        schedule.setVendorName(request.getVendorName());
        if (request.getScheduledDate() != null) schedule.setScheduledDate(request.getScheduledDate());
        schedule.setVehicleNumber(request.getVehicleNumber());
        schedule.setDriverName(request.getDriverName());
        schedule.setDriverPhone(request.getDriverPhone());
        schedule.setWarehouseId(request.getWarehouseId());
        schedule.setWarehouseName(request.getWarehouseName());
        schedule.setNotes(request.getNotes());
        if (request.getItems() != null) {
            schedule.getItems().clear();
            List<IntakeScheduleItem> items = buildItems(schedule, tenantId, request.getItems());
            schedule.getItems().addAll(items);
            BigDecimal totalExpected = items.stream()
                    .map(IntakeScheduleItem::getExpectedQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            schedule.setExpectedQuantity(request.getExpectedQuantity() != null ? request.getExpectedQuantity() : totalExpected);
        } else if (request.getExpectedQuantity() != null) {
            schedule.setExpectedQuantity(request.getExpectedQuantity());
        }
        schedule = intakeScheduleRepository.save(schedule);
        log.info("IntakeSchedule updated: id={}", id);
        return toDto(schedule);
    }

    @Transactional
    public IntakeScheduleDto updateStatus(UUID id, ScheduleStatus status) {
        IntakeSchedule schedule = findOrThrow(id);
        schedule.setStatus(status);
        return toDto(intakeScheduleRepository.save(schedule));
    }

    @Transactional
    public void delete(UUID id) {
        IntakeSchedule schedule = findOrThrow(id);
        if (schedule.getStatus() != ScheduleStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT schedules can be deleted");
        }
        schedule.setDeletedAt(LocalDateTime.now());
        intakeScheduleRepository.save(schedule);
        log.info("IntakeSchedule soft-deleted: id={}", id);
    }

    private List<IntakeScheduleItem> buildItems(IntakeSchedule schedule, UUID tenantId,
            List<CreateIntakeScheduleRequest.ItemRequest> requests) {
        if (requests == null) return new ArrayList<>();
        return requests.stream().map(r -> {
            IntakeScheduleItem item = new IntakeScheduleItem();
            item.setTenantId(tenantId);
            item.setSchedule(schedule);
            item.setProductId(r.getProductId());
            item.setProductName(r.getProductName());
            item.setExpectedQuantity(r.getExpectedQuantity() != null ? r.getExpectedQuantity() : BigDecimal.ZERO);
            item.setActualQuantity(BigDecimal.ZERO);
            item.setUnit(r.getUnit());
            item.setPurchaseOrderId(r.getPurchaseOrderId());
            item.setNotes(r.getNotes());
            return item;
        }).collect(Collectors.toList());
    }

    private IntakeScheduleDto toDto(IntakeSchedule schedule) {
        IntakeScheduleDto dto = new IntakeScheduleDto();
        dto.setId(schedule.getId());
        dto.setTenantId(schedule.getTenantId());
        dto.setScheduleNumber(schedule.getScheduleNumber());
        dto.setVendorId(schedule.getVendorId());
        dto.setVendorName(schedule.getVendorName());
        dto.setScheduledDate(schedule.getScheduledDate());
        dto.setStatus(schedule.getStatus());
        dto.setVehicleNumber(schedule.getVehicleNumber());
        dto.setDriverName(schedule.getDriverName());
        dto.setDriverPhone(schedule.getDriverPhone());
        dto.setExpectedQuantity(schedule.getExpectedQuantity());
        dto.setActualQuantity(schedule.getActualQuantity());
        dto.setWarehouseId(schedule.getWarehouseId());
        dto.setWarehouseName(schedule.getWarehouseName());
        dto.setNotes(schedule.getNotes());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setUpdatedAt(schedule.getUpdatedAt());

        if (schedule.getItems() != null) {
            dto.setItems(schedule.getItems().stream().map(item -> {
                IntakeScheduleDto.ItemDto idto = new IntakeScheduleDto.ItemDto();
                idto.setId(item.getId());
                idto.setProductId(item.getProductId());
                idto.setProductName(item.getProductName());
                idto.setExpectedQuantity(item.getExpectedQuantity());
                idto.setActualQuantity(item.getActualQuantity());
                idto.setUnit(item.getUnit());
                idto.setPurchaseOrderId(item.getPurchaseOrderId());
                idto.setNotes(item.getNotes());
                return idto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private IntakeSchedule findOrThrow(UUID id) {
        return intakeScheduleRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Intake schedule not found: " + id));
    }

    private String generateScheduleNumber(UUID tenantId) {
        long count = intakeScheduleRepository.countByTenantIdAndDeletedAtIsNull(tenantId);
        String year = String.valueOf(Year.now().getValue());
        return String.format("IS-%s-%03d", year, count + 1);
    }
}

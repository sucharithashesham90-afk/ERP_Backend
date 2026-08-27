package com.erp.platform.modules.sales.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.sales.dto.CreateDemandForecastRequest;
import com.erp.platform.modules.sales.dto.DemandForecastDto;
import com.erp.platform.modules.sales.entity.DemandForecast;
import com.erp.platform.modules.sales.entity.DemandForecast.ForecastStatus;
import com.erp.platform.modules.sales.repository.DemandForecastRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DemandForecastService {

    private final DemandForecastRepository forecastRepository;
    private final TenantContext tenantContext;

    public PageResponse<DemandForecastDto> list(UUID productId, int year, ForecastStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<DemandForecast> page;
        if (productId != null) {
            page = forecastRepository.findByTenantIdAndProductIdAndDeletedAtIsNull(tenantId, productId, pageable);
        } else if (status != null) {
            page = forecastRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else if (year > 0) {
            page = forecastRepository.findByTenantIdAndForecastYearAndDeletedAtIsNull(tenantId, year, pageable);
        } else {
            page = forecastRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.of(page.map(this::toDto));
    }

    public DemandForecastDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public DemandForecastDto create(CreateDemandForecastRequest request) {
        UUID tenantId = tenantContext.current();
        long count = forecastRepository.countByTenantId(tenantId);
        String forecastNumber = String.format("DF-%d-%03d", Year.now().getValue(), count + 1);

        DemandForecast forecast = new DemandForecast();
        forecast.setTenantId(tenantId);
        forecast.setForecastNumber(forecastNumber);
        forecast.setProductId(request.getProductId());
        forecast.setProductName(request.getProductName());
        forecast.setForecastPeriod(request.getForecastPeriod());
        forecast.setForecastYear(request.getForecastYear());
        forecast.setForecastMonth(request.getForecastMonth());
        forecast.setForecastQuarter(request.getForecastQuarter());
        forecast.setForecastMethod(request.getForecastMethod());
        forecast.setForecastedQty(request.getForecastedQty());
        BigDecimal actualQty = request.getActualQty() != null ? request.getActualQty() : BigDecimal.ZERO;
        forecast.setActualQty(actualQty);
        forecast.setVariance(actualQty.subtract(request.getForecastedQty()));
        forecast.setConfidenceLevel(request.getConfidenceLevel());
        forecast.setWarehouseId(request.getWarehouseId());
        forecast.setUnit(request.getUnit());
        forecast.setStatus(ForecastStatus.DRAFT);
        forecast.setNotes(request.getNotes());

        forecast = forecastRepository.save(forecast);
        log.info("DemandForecast created: id={}, forecastNumber={}", forecast.getId(), forecast.getForecastNumber());
        return toDto(forecast);
    }

    @Transactional
    public DemandForecastDto update(UUID id, CreateDemandForecastRequest request) {
        DemandForecast forecast = findOrThrow(id);

        forecast.setProductId(request.getProductId());
        forecast.setProductName(request.getProductName());
        forecast.setForecastPeriod(request.getForecastPeriod());
        forecast.setForecastYear(request.getForecastYear());
        forecast.setForecastMonth(request.getForecastMonth());
        forecast.setForecastQuarter(request.getForecastQuarter());
        forecast.setForecastMethod(request.getForecastMethod());
        forecast.setForecastedQty(request.getForecastedQty());
        BigDecimal actualQty = request.getActualQty() != null ? request.getActualQty() : BigDecimal.ZERO;
        forecast.setActualQty(actualQty);
        forecast.setVariance(actualQty.subtract(request.getForecastedQty()));
        forecast.setConfidenceLevel(request.getConfidenceLevel());
        forecast.setWarehouseId(request.getWarehouseId());
        forecast.setUnit(request.getUnit());
        forecast.setNotes(request.getNotes());

        return toDto(forecastRepository.save(forecast));
    }

    @Transactional
    public DemandForecastDto publish(UUID id) {
        DemandForecast forecast = findOrThrow(id);
        if (forecast.getStatus() != ForecastStatus.DRAFT) {
            throw AppException.businessRule("Only DRAFT forecasts can be published. Current status: " + forecast.getStatus());
        }
        forecast.setStatus(ForecastStatus.PUBLISHED);
        return toDto(forecastRepository.save(forecast));
    }

    @Transactional
    public DemandForecastDto archive(UUID id) {
        DemandForecast forecast = findOrThrow(id);
        if (forecast.getStatus() == ForecastStatus.ARCHIVED) {
            throw AppException.businessRule("Forecast is already ARCHIVED");
        }
        forecast.setStatus(ForecastStatus.ARCHIVED);
        return toDto(forecastRepository.save(forecast));
    }

    @Transactional
    public void delete(UUID id) {
        DemandForecast forecast = findOrThrow(id);
        forecast.setDeletedAt(LocalDateTime.now());
        forecastRepository.save(forecast);
        log.info("DemandForecast soft-deleted: id={}", id);
    }

    private DemandForecast findOrThrow(UUID id) {
        return forecastRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Demand forecast not found: " + id));
    }

    private DemandForecastDto toDto(DemandForecast f) {
        DemandForecastDto dto = new DemandForecastDto();
        dto.setId(f.getId());
        dto.setTenantId(f.getTenantId());
        dto.setForecastNumber(f.getForecastNumber());
        dto.setProductId(f.getProductId());
        dto.setProductName(f.getProductName());
        dto.setForecastPeriod(f.getForecastPeriod());
        dto.setForecastYear(f.getForecastYear());
        dto.setForecastMonth(f.getForecastMonth());
        dto.setForecastQuarter(f.getForecastQuarter());
        dto.setForecastMethod(f.getForecastMethod());
        dto.setForecastedQty(f.getForecastedQty());
        dto.setActualQty(f.getActualQty());
        dto.setVariance(f.getVariance());
        dto.setConfidenceLevel(f.getConfidenceLevel());
        dto.setWarehouseId(f.getWarehouseId());
        dto.setUnit(f.getUnit());
        dto.setStatus(f.getStatus());
        dto.setNotes(f.getNotes());
        dto.setCreatedAt(f.getCreatedAt());
        return dto;
    }
}

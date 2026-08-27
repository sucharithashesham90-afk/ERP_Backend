package com.erp.platform.modules.fieldiot.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.fieldiot.dto.FieldIotDtos.*;
import com.erp.platform.modules.fieldiot.entity.FieldPlot;
import com.erp.platform.modules.fieldiot.entity.FieldReading;
import com.erp.platform.modules.fieldiot.entity.FieldReading.ReadingKind;
import com.erp.platform.modules.fieldiot.entity.IotDevice;
import com.erp.platform.modules.fieldiot.repository.FieldPlotRepository;
import com.erp.platform.modules.fieldiot.repository.FieldReadingRepository;
import com.erp.platform.modules.fieldiot.repository.IotDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Field mapping, device registry, observation ingest and the dashboard that reads them.
 *
 * Thresholds below are agronomic rules of thumb, deliberately kept in one place so they can be
 * moved to per-crop configuration later without hunting them down across the module.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FieldIotService {

    private static final BigDecimal SOIL_MOISTURE_CRITICAL = BigDecimal.valueOf(15);
    private static final BigDecimal SOIL_MOISTURE_LOW      = BigDecimal.valueOf(22);
    private static final BigDecimal NDVI_CRITICAL          = BigDecimal.valueOf(0.30);
    private static final BigDecimal NDVI_LOW               = BigDecimal.valueOf(0.45);
    private static final BigDecimal HEAT_STRESS_C          = BigDecimal.valueOf(38);
    private static final BigDecimal HEAVY_RAIN_MM          = BigDecimal.valueOf(25);
    private static final BigDecimal PH_ACIDIC              = BigDecimal.valueOf(5.5);
    private static final BigDecimal PH_ALKALINE            = BigDecimal.valueOf(8.2);

    /** A device silent for longer than this is treated as offline. */
    private static final long DEVICE_OFFLINE_HOURS = 24;
    private static final long DEVICE_STALE_HOURS = 6;

    private final FieldPlotRepository fieldPlotRepository;
    private final IotDeviceRepository deviceRepository;
    private final FieldReadingRepository readingRepository;
    private final FieldDataFeed dataFeed;
    private final TenantContext tenantContext;

    // ── Fields ───────────────────────────────────────────────────────────────

    public PageResponse<FieldPlotDto> listFields(Pageable pageable) {
        return PageResponse.of(fieldPlotRepository
                .findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public FieldPlotDto getField(UUID id) {
        return toDto(findFieldOrThrow(id));
    }

    @Transactional
    public FieldPlotDto createField(FieldPlotRequest request) {
        UUID tenantId = tenantContext.current();
        if (request.getName() == null || request.getName().isBlank()) {
            throw AppException.badRequest("Field name is required");
        }
        FieldPlot field = new FieldPlot();
        field.setTenantId(tenantId);
        applyField(field, request);
        if (field.getCode() == null || field.getCode().isBlank()) {
            field.setCode(String.format("FLD-%04d", fieldPlotRepository.countByTenantIdAndDeletedAtIsNull(tenantId) + 1));
        }
        FieldPlot saved = fieldPlotRepository.save(field);
        log.info("Field plot created: id={}, name={}", saved.getId(), saved.getName());
        return toDto(saved);
    }

    @Transactional
    public FieldPlotDto updateField(UUID id, FieldPlotRequest request) {
        FieldPlot field = findFieldOrThrow(id);
        applyField(field, request);
        return toDto(fieldPlotRepository.save(field));
    }

    @Transactional
    public void deleteField(UUID id) {
        FieldPlot field = findFieldOrThrow(id);
        field.setDeletedAt(LocalDateTime.now());
        fieldPlotRepository.save(field);
    }

    // ── Devices ──────────────────────────────────────────────────────────────

    public PageResponse<IotDeviceDto> listDevices(Pageable pageable) {
        return PageResponse.of(deviceRepository
                .findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    @Transactional
    public IotDeviceDto createDevice(IotDeviceRequest request) {
        UUID tenantId = tenantContext.current();
        if (request.getDeviceCode() == null || request.getDeviceCode().isBlank()) {
            throw AppException.badRequest("Device code is required — it is how readings are matched to a device");
        }
        deviceRepository.findByTenantIdAndDeviceCodeIgnoreCaseAndDeletedAtIsNull(tenantId, request.getDeviceCode())
                .ifPresent(d -> { throw AppException.badRequest("Device code already registered: " + d.getDeviceCode()); });

        IotDevice device = new IotDevice();
        device.setTenantId(tenantId);
        applyDevice(device, request);
        return toDto(deviceRepository.save(device));
    }

    @Transactional
    public IotDeviceDto updateDevice(UUID id, IotDeviceRequest request) {
        IotDevice device = deviceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Device not found: " + id));
        applyDevice(device, request);
        return toDto(deviceRepository.save(device));
    }

    @Transactional
    public void deleteDevice(UUID id) {
        IotDevice device = deviceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Device not found: " + id));
        device.setDeletedAt(LocalDateTime.now());
        deviceRepository.save(device);
    }

    // ── Ingest ───────────────────────────────────────────────────────────────

    /**
     * Accepts a reading pushed by a field device. The device's own numbers win over anything the
     * sync would have generated, so a site with real probes stops depending on the feed entirely.
     */
    @Transactional
    public FieldReadingDto ingest(IngestRequest request) {
        UUID tenantId = tenantContext.current();

        IotDevice device = null;
        if (request.getDeviceCode() != null && !request.getDeviceCode().isBlank()) {
            device = deviceRepository
                    .findByTenantIdAndDeviceCodeIgnoreCaseAndDeletedAtIsNull(tenantId, request.getDeviceCode())
                    .orElseThrow(() -> AppException.notFound(
                            "Unknown device code: " + request.getDeviceCode() + " — register the device first"));
        }

        UUID fieldId = request.getFieldPlotId() != null ? request.getFieldPlotId()
                : device != null ? device.getFieldPlotId() : null;
        if (fieldId == null) {
            throw AppException.badRequest("Reading must name a field, directly or through a device assigned to one");
        }
        FieldPlot field = findFieldOrThrow(fieldId);

        boolean hasSoil = request.getSoilMoisturePercent() != null || request.getSoilPh() != null
                || request.getSoilTemperatureC() != null || request.getNitrogenPpm() != null
                || request.getPhosphorusPpm() != null || request.getPotassiumPpm() != null
                || request.getSoilEc() != null;
        boolean hasWeather = request.getTemperatureC() != null || request.getHumidityPercent() != null
                || request.getRainfallMm() != null || request.getWindSpeedKph() != null;
        if (!hasSoil && !hasWeather) {
            throw AppException.badRequest("Reading carries no measurements");
        }

        FieldReading r = new FieldReading();
        r.setTenantId(tenantId);
        r.setKind(hasSoil ? ReadingKind.SOIL : ReadingKind.WEATHER);
        r.setFieldPlotId(field.getId());
        r.setFieldPlotName(field.getName());
        r.setObservedAt(request.getObservedAt() != null ? request.getObservedAt() : LocalDateTime.now());
        r.setSource(device != null ? device.getDeviceCode() : "DEVICE");
        r.setDeviceCode(device != null ? device.getDeviceCode() : request.getDeviceCode());
        r.setTemperatureC(request.getTemperatureC());
        r.setHumidityPercent(request.getHumidityPercent());
        r.setRainfallMm(request.getRainfallMm());
        r.setWindSpeedKph(request.getWindSpeedKph());
        r.setSoilMoisturePercent(request.getSoilMoisturePercent());
        r.setSoilTemperatureC(request.getSoilTemperatureC());
        r.setSoilPh(request.getSoilPh());
        r.setSoilEc(request.getSoilEc());
        r.setNitrogenPpm(request.getNitrogenPpm());
        r.setPhosphorusPpm(request.getPhosphorusPpm());
        r.setPotassiumPpm(request.getPotassiumPpm());
        FieldReading saved = readingRepository.save(r);

        if (device != null) {
            device.setLastSeenAt(saved.getObservedAt());
            if (request.getBatteryPercent() != null) device.setBatteryPercent(request.getBatteryPercent());
            if (request.getSignalPercent() != null) device.setSignalPercent(request.getSignalPercent());
            deviceRepository.save(device);
        }
        return toDto(saved);
    }

    // ── Sync ─────────────────────────────────────────────────────────────────

    /** Pulls a fresh weather, soil and satellite observation for every active field. */
    @Transactional
    public SyncResultDto sync() {
        return sync(tenantContext.current());
    }

    /**
     * Sync one tenant's fields, named explicitly rather than read from the request.
     *
     * <p>The scheduled sync runs on a plain thread with no request behind it, and TenantContext is
     * request-scoped — so the tenant has to be passed in rather than looked up.
     */
    public SyncResultDto sync(UUID tenantId) {
        LocalDateTime now = LocalDateTime.now();

        SyncResultDto result = new SyncResultDto();
        result.setSyncedAt(now);
        result.setSimulated(!dataFeed.isLive());
        result.setProviderStatus(dataFeed.providerStatus());

        List<FieldPlot> fields = fieldPlotRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        if (fields.isEmpty()) {
            result.getMessages().add("No active fields mapped — add a field before syncing");
            return result;
        }

        for (FieldPlot field : fields) {
            for (FieldReading reading : dataFeed.fetch(field, now, result.getMessages())) {
                readingRepository.save(reading);
                switch (reading.getKind()) {
                    case WEATHER -> result.setWeatherReadings(result.getWeatherReadings() + 1);
                    case SOIL -> result.setSoilReadings(result.getSoilReadings() + 1);
                    case SATELLITE -> result.setSatelliteReadings(result.getSatelliteReadings() + 1);
                }
            }
            result.setFieldsSynced(result.getFieldsSynced() + 1);
        }
        log.info("Field IoT sync complete: {} field(s), {} reading(s), simulated={}",
                result.getFieldsSynced(),
                result.getWeatherReadings() + result.getSoilReadings() + result.getSatelliteReadings(),
                result.isSimulated());
        return result;
    }

    // ── Dashboard ────────────────────────────────────────────────────────────

    public DashboardDto dashboard() {
        UUID tenantId = tenantContext.current();
        LocalDateTime now = LocalDateTime.now();

        DashboardDto out = new DashboardDto();
        out.setSimulated(!dataFeed.isLive());
        out.setProviderStatus(dataFeed.providerStatus());
        out.setReadingCount(readingRepository.countByTenantIdAndDeletedAtIsNull(tenantId));

        List<FieldPlot> fields = fieldPlotRepository.findByTenantIdAndDeletedAtIsNull(tenantId);
        List<IotDevice> devices = deviceRepository.findByTenantIdAndDeletedAtIsNull(tenantId);

        out.setFieldCount(fields.size());
        out.setDeviceCount(devices.size());
        for (IotDevice d : devices) {
            if ("OFFLINE".equals(deviceHealth(d, now))) out.setDevicesOffline(out.getDevicesOffline() + 1);
            else out.setDevicesOnline(out.getDevicesOnline() + 1);
        }

        BigDecimal ndviTotal = BigDecimal.ZERO;
        int ndviCount = 0;
        BigDecimal moistureTotal = BigDecimal.ZERO;
        int moistureCount = 0;

        for (FieldPlot field : fields) {
            FieldSnapshotDto snap = new FieldSnapshotDto();
            snap.setField(toDto(field));
            snap.setLatestWeather(latest(tenantId, field.getId(), ReadingKind.WEATHER));
            snap.setLatestSoil(latest(tenantId, field.getId(), ReadingKind.SOIL));
            snap.setLatestSatellite(latest(tenantId, field.getId(), ReadingKind.SATELLITE));

            List<IotDevice> fieldDevices = devices.stream()
                    .filter(d -> field.getId().equals(d.getFieldPlotId())).toList();
            snap.setDeviceCount(fieldDevices.size());
            snap.setDevicesOffline((int) fieldDevices.stream()
                    .filter(d -> "OFFLINE".equals(deviceHealth(d, now))).count());

            snap.setAlerts(buildAlerts(snap));
            for (Alert a : snap.getAlerts()) {
                if ("CRITICAL".equals(a.getSeverity())) out.setCriticalAlerts(out.getCriticalAlerts() + 1);
                else if ("WARNING".equals(a.getSeverity())) out.setWarningAlerts(out.getWarningAlerts() + 1);
            }

            if (field.getAreaHectares() != null) {
                out.setTotalAreaHectares(out.getTotalAreaHectares().add(field.getAreaHectares()));
            }
            if (snap.getLatestSatellite() != null && snap.getLatestSatellite().getNdvi() != null) {
                ndviTotal = ndviTotal.add(snap.getLatestSatellite().getNdvi());
                ndviCount++;
            }
            if (snap.getLatestSoil() != null && snap.getLatestSoil().getSoilMoisturePercent() != null) {
                moistureTotal = moistureTotal.add(snap.getLatestSoil().getSoilMoisturePercent());
                moistureCount++;
            }
            if (out.getLastSyncAt() == null || isNewer(snap, out.getLastSyncAt())) {
                out.setLastSyncAt(mostRecentObservation(snap, out.getLastSyncAt()));
            }
            out.getFields().add(snap);
        }

        if (ndviCount > 0) out.setAverageNdvi(ndviTotal.divide(BigDecimal.valueOf(ndviCount), 3, RoundingMode.HALF_UP));
        if (moistureCount > 0) out.setAverageSoilMoisture(
                moistureTotal.divide(BigDecimal.valueOf(moistureCount), 1, RoundingMode.HALF_UP));
        return out;
    }

    public PageResponse<FieldReadingDto> listReadings(UUID fieldPlotId, ReadingKind kind, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (fieldPlotId != null && kind != null) {
            return PageResponse.of(readingRepository
                    .findByTenantIdAndFieldPlotIdAndKindAndDeletedAtIsNullOrderByObservedAtDesc(
                            tenantId, fieldPlotId, kind, pageable).map(this::toDto));
        }
        if (fieldPlotId != null) {
            return PageResponse.of(readingRepository
                    .findByTenantIdAndFieldPlotIdAndDeletedAtIsNullOrderByObservedAtDesc(
                            tenantId, fieldPlotId, pageable).map(this::toDto));
        }
        if (kind != null) {
            return PageResponse.of(readingRepository
                    .findByTenantIdAndKindAndDeletedAtIsNullOrderByObservedAtDesc(tenantId, kind, pageable)
                    .map(this::toDto));
        }
        return PageResponse.of(readingRepository
                .findByTenantIdAndDeletedAtIsNullOrderByObservedAtDesc(tenantId, pageable).map(this::toDto));
    }

    // ── Alerts ───────────────────────────────────────────────────────────────

    private List<Alert> buildAlerts(FieldSnapshotDto snap) {
        List<Alert> alerts = new ArrayList<>();
        FieldReadingDto soil = snap.getLatestSoil();
        FieldReadingDto weather = snap.getLatestWeather();
        FieldReadingDto sat = snap.getLatestSatellite();

        if (soil != null && soil.getSoilMoisturePercent() != null) {
            BigDecimal m = soil.getSoilMoisturePercent();
            if (m.compareTo(SOIL_MOISTURE_CRITICAL) < 0) {
                alerts.add(new Alert("CRITICAL", "soilMoisture",
                        "Soil moisture " + m + "% — irrigate now"));
            } else if (m.compareTo(SOIL_MOISTURE_LOW) < 0) {
                alerts.add(new Alert("WARNING", "soilMoisture",
                        "Soil moisture " + m + "% — irrigation due soon"));
            }
        }
        if (soil != null && soil.getSoilPh() != null) {
            if (soil.getSoilPh().compareTo(PH_ACIDIC) < 0) {
                alerts.add(new Alert("WARNING", "soilPh", "Soil pH " + soil.getSoilPh() + " — acidic, consider liming"));
            } else if (soil.getSoilPh().compareTo(PH_ALKALINE) > 0) {
                alerts.add(new Alert("WARNING", "soilPh", "Soil pH " + soil.getSoilPh() + " — alkaline, nutrient lock-up risk"));
            }
        }
        if (weather != null && weather.getTemperatureC() != null
                && weather.getTemperatureC().compareTo(HEAT_STRESS_C) > 0) {
            alerts.add(new Alert("WARNING", "temperature",
                    "Air temperature " + weather.getTemperatureC() + "°C — heat stress likely"));
        }
        if (weather != null && weather.getRainfallMm() != null
                && weather.getRainfallMm().compareTo(HEAVY_RAIN_MM) > 0) {
            alerts.add(new Alert("INFO", "rainfall",
                    "Rainfall " + weather.getRainfallMm() + "mm — hold off irrigating and check drainage"));
        }
        if (sat != null && sat.getNdvi() != null) {
            if (sat.getNdvi().compareTo(NDVI_CRITICAL) < 0) {
                alerts.add(new Alert("CRITICAL", "ndvi",
                        "NDVI " + sat.getNdvi() + " — canopy stressed or sparse, inspect the field"));
            } else if (sat.getNdvi().compareTo(NDVI_LOW) < 0) {
                alerts.add(new Alert("WARNING", "ndvi",
                        "NDVI " + sat.getNdvi() + " — vigour below par"));
            }
        }
        if (snap.getDevicesOffline() > 0) {
            alerts.add(new Alert("WARNING", "device",
                    snap.getDevicesOffline() + " device(s) offline — readings may be stale"));
        }
        return alerts;
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private FieldReadingDto latest(UUID tenantId, UUID fieldId, ReadingKind kind) {
        var page = readingRepository.findByTenantIdAndFieldPlotIdAndKindAndDeletedAtIsNullOrderByObservedAtDesc(
                tenantId, fieldId, kind, PageRequest.of(0, 1));
        return page.hasContent() ? toDto(page.getContent().get(0)) : null;
    }

    private boolean isNewer(FieldSnapshotDto snap, LocalDateTime current) {
        LocalDateTime candidate = mostRecentObservation(snap, null);
        return candidate != null && (current == null || candidate.isAfter(current));
    }

    private LocalDateTime mostRecentObservation(FieldSnapshotDto snap, LocalDateTime current) {
        LocalDateTime best = current;
        for (FieldReadingDto r : new FieldReadingDto[]{
                snap.getLatestWeather(), snap.getLatestSoil(), snap.getLatestSatellite()}) {
            if (r != null && r.getObservedAt() != null && (best == null || r.getObservedAt().isAfter(best))) {
                best = r.getObservedAt();
            }
        }
        return best;
    }

    private String deviceHealth(IotDevice d, LocalDateTime now) {
        if (!d.isActive()) return "OFFLINE";
        if (d.getLastSeenAt() == null) return "OFFLINE";
        long hours = java.time.Duration.between(d.getLastSeenAt(), now).toHours();
        if (hours >= DEVICE_OFFLINE_HOURS) return "OFFLINE";
        if (hours >= DEVICE_STALE_HOURS) return "STALE";
        return "ONLINE";
    }

    private void applyField(FieldPlot field, FieldPlotRequest r) {
        if (r.getName() != null) field.setName(r.getName());
        if (r.getCode() != null && !r.getCode().isBlank()) field.setCode(r.getCode());
        field.setLocation(r.getLocation());
        field.setGrowerId(r.getGrowerId());
        field.setGrowerName(r.getGrowerName());
        field.setCropName(r.getCropName());
        field.setVarietyName(r.getVarietyName());
        field.setAreaHectares(r.getAreaHectares());
        field.setCentroidLatitude(r.getCentroidLatitude());
        field.setCentroidLongitude(r.getCentroidLongitude());
        field.setBoundaryGeojson(r.getBoundaryGeojson());
        field.setSowingDate(r.getSowingDate());
        field.setExpectedHarvestDate(r.getExpectedHarvestDate());
        if (r.getActive() != null) field.setActive(r.getActive());
        field.setNotes(r.getNotes());
    }

    private void applyDevice(IotDevice device, IotDeviceRequest r) {
        if (r.getDeviceCode() != null) device.setDeviceCode(r.getDeviceCode());
        if (r.getName() != null) device.setName(r.getName());
        device.setDeviceType(r.getDeviceType());
        device.setFieldPlotId(r.getFieldPlotId());
        device.setFieldPlotName(r.getFieldPlotId() == null ? null
                : fieldPlotRepository.findByTenantIdAndIdAndDeletedAtIsNull(device.getTenantId(), r.getFieldPlotId())
                        .map(FieldPlot::getName).orElse(null));
        if (r.getActive() != null) device.setActive(r.getActive());
        device.setNotes(r.getNotes());
    }

    private FieldPlot findFieldOrThrow(UUID id) {
        return fieldPlotRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Field not found: " + id));
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private FieldPlotDto toDto(FieldPlot f) {
        FieldPlotDto d = new FieldPlotDto();
        d.setId(f.getId());
        d.setName(f.getName());
        d.setCode(f.getCode());
        d.setLocation(f.getLocation());
        d.setGrowerId(f.getGrowerId());
        d.setGrowerName(f.getGrowerName());
        d.setCropName(f.getCropName());
        d.setVarietyName(f.getVarietyName());
        d.setAreaHectares(f.getAreaHectares());
        d.setCentroidLatitude(f.getCentroidLatitude());
        d.setCentroidLongitude(f.getCentroidLongitude());
        d.setBoundaryGeojson(f.getBoundaryGeojson());
        d.setSowingDate(f.getSowingDate());
        d.setExpectedHarvestDate(f.getExpectedHarvestDate());
        d.setActive(f.isActive());
        d.setNotes(f.getNotes());
        d.setCreatedAt(f.getCreatedAt());
        return d;
    }

    private IotDeviceDto toDto(IotDevice v) {
        IotDeviceDto d = new IotDeviceDto();
        d.setId(v.getId());
        d.setDeviceCode(v.getDeviceCode());
        d.setName(v.getName());
        d.setDeviceType(v.getDeviceType());
        d.setFieldPlotId(v.getFieldPlotId());
        d.setFieldPlotName(v.getFieldPlotName());
        d.setLastSeenAt(v.getLastSeenAt());
        d.setBatteryPercent(v.getBatteryPercent());
        d.setSignalPercent(v.getSignalPercent());
        d.setActive(v.isActive());
        d.setNotes(v.getNotes());
        d.setHealth(deviceHealth(v, LocalDateTime.now()));
        return d;
    }

    private FieldReadingDto toDto(FieldReading r) {
        FieldReadingDto d = new FieldReadingDto();
        d.setId(r.getId());
        d.setKind(r.getKind());
        d.setFieldPlotId(r.getFieldPlotId());
        d.setFieldPlotName(r.getFieldPlotName());
        d.setObservedAt(r.getObservedAt());
        d.setSource(r.getSource());
        d.setDeviceCode(r.getDeviceCode());
        d.setTemperatureC(r.getTemperatureC());
        d.setHumidityPercent(r.getHumidityPercent());
        d.setRainfallMm(r.getRainfallMm());
        d.setWindSpeedKph(r.getWindSpeedKph());
        d.setWeatherSummary(r.getWeatherSummary());
        d.setSoilMoisturePercent(r.getSoilMoisturePercent());
        d.setSoilTemperatureC(r.getSoilTemperatureC());
        d.setSoilPh(r.getSoilPh());
        d.setSoilEc(r.getSoilEc());
        d.setNitrogenPpm(r.getNitrogenPpm());
        d.setPhosphorusPpm(r.getPhosphorusPpm());
        d.setPotassiumPpm(r.getPotassiumPpm());
        d.setNdvi(r.getNdvi());
        d.setEvi(r.getEvi());
        d.setCloudCoverPercent(r.getCloudCoverPercent());
        d.setSceneId(r.getSceneId());
        return d;
    }
}

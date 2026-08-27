package com.erp.platform.modules.fieldiot.dto;

import com.erp.platform.modules.fieldiot.entity.FieldReading.ReadingKind;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Transport types for the satellite &amp; IoT module, grouped since none stands alone. */
public final class FieldIotDtos {

    private FieldIotDtos() {}

    @Data
    public static class FieldPlotDto {
        private UUID id;
        private String name;
        private String code;
        private String location;
        private UUID growerId;
        private String growerName;
        private String cropName;
        private String varietyName;
        private BigDecimal areaHectares;
        private BigDecimal centroidLatitude;
        private BigDecimal centroidLongitude;
        private String boundaryGeojson;
        private LocalDate sowingDate;
        private LocalDate expectedHarvestDate;
        private boolean active;
        private String notes;
        private LocalDateTime createdAt;
    }

    @Data
    public static class IotDeviceDto {
        private UUID id;
        private String deviceCode;
        private String name;
        private String deviceType;
        private UUID fieldPlotId;
        private String fieldPlotName;
        private LocalDateTime lastSeenAt;
        private Integer batteryPercent;
        private Integer signalPercent;
        private boolean active;
        private String notes;
        /** ONLINE, STALE or OFFLINE, derived from lastSeenAt. */
        private String health;
    }

    @Data
    public static class FieldReadingDto {
        private UUID id;
        private ReadingKind kind;
        private UUID fieldPlotId;
        private String fieldPlotName;
        private LocalDateTime observedAt;
        private String source;
        private String deviceCode;

        private BigDecimal temperatureC;
        private BigDecimal humidityPercent;
        private BigDecimal rainfallMm;
        private BigDecimal windSpeedKph;
        private String weatherSummary;

        private BigDecimal soilMoisturePercent;
        private BigDecimal soilTemperatureC;
        private BigDecimal soilPh;
        private BigDecimal soilEc;
        private BigDecimal nitrogenPpm;
        private BigDecimal phosphorusPpm;
        private BigDecimal potassiumPpm;

        private BigDecimal ndvi;
        private BigDecimal evi;
        private BigDecimal cloudCoverPercent;
        private String sceneId;
    }

    /** One card on the dashboard: a field plus the freshest reading from each feed. */
    @Data
    public static class FieldSnapshotDto {
        private FieldPlotDto field;
        private FieldReadingDto latestWeather;
        private FieldReadingDto latestSoil;
        private FieldReadingDto latestSatellite;
        private int deviceCount;
        private int devicesOffline;
        /** Plain-language conditions worth acting on, e.g. low soil moisture. */
        private List<Alert> alerts = new ArrayList<>();
    }

    @Data
    public static class Alert {
        private String severity;   // CRITICAL | WARNING | INFO
        private String metric;
        private String message;

        public Alert() {}

        public Alert(String severity, String metric, String message) {
            this.severity = severity;
            this.metric = metric;
            this.message = message;
        }
    }

    @Data
    public static class DashboardDto {
        private int fieldCount;
        private BigDecimal totalAreaHectares = BigDecimal.ZERO;
        private int deviceCount;
        private int devicesOnline;
        private int devicesOffline;
        private long readingCount;
        private LocalDateTime lastSyncAt;
        /** True when readings came from the built-in simulator, not a configured provider. */
        private boolean simulated;
        private String providerStatus;
        private int criticalAlerts;
        private int warningAlerts;
        private BigDecimal averageNdvi;
        private BigDecimal averageSoilMoisture;
        private List<FieldSnapshotDto> fields = new ArrayList<>();
    }

    @Data
    public static class SyncResultDto {
        private int fieldsSynced;
        private int weatherReadings;
        private int soilReadings;
        private int satelliteReadings;
        private boolean simulated;
        private String providerStatus;
        private LocalDateTime syncedAt;
        private List<String> messages = new ArrayList<>();
    }

    @Data
    public static class FieldPlotRequest {
        private String name;
        private String code;
        private String location;
        private UUID growerId;
        private String growerName;
        private String cropName;
        private String varietyName;
        private BigDecimal areaHectares;
        private BigDecimal centroidLatitude;
        private BigDecimal centroidLongitude;
        private String boundaryGeojson;
        private LocalDate sowingDate;
        private LocalDate expectedHarvestDate;
        private Boolean active;
        private String notes;
    }

    @Data
    public static class IotDeviceRequest {
        private String deviceCode;
        private String name;
        private String deviceType;
        private UUID fieldPlotId;
        private Boolean active;
        private String notes;
    }

    /** Payload a field device posts directly, without going through a cloud provider. */
    @Data
    public static class IngestRequest {
        private String deviceCode;
        private UUID fieldPlotId;
        private LocalDateTime observedAt;
        private Integer batteryPercent;
        private Integer signalPercent;

        private BigDecimal temperatureC;
        private BigDecimal humidityPercent;
        private BigDecimal rainfallMm;
        private BigDecimal windSpeedKph;

        private BigDecimal soilMoisturePercent;
        private BigDecimal soilTemperatureC;
        private BigDecimal soilPh;
        private BigDecimal soilEc;
        private BigDecimal nitrogenPpm;
        private BigDecimal phosphorusPpm;
        private BigDecimal potassiumPpm;
    }
}

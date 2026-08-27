package com.erp.platform.modules.fieldiot.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One observation for a field, from any of the three feeds.
 *
 * Weather, soil and satellite share a table because they share every access pattern the module
 * has — latest per field, series per field, per-field aggregates — and splitting them into three
 * near-identical tables would triple the queries behind every dashboard tile for no gain. The
 * {@link ReadingKind} discriminator keeps them apart; each feed populates its own columns.
 */
@Entity(name = "IotFieldReading")
@Table(name = "field_readings",
       indexes = {
           @Index(name = "idx_fieldreading_tenant", columnList = "tenant_id"),
           @Index(name = "idx_fieldreading_field_kind", columnList = "tenant_id, field_plot_id, kind"),
           @Index(name = "idx_fieldreading_observed", columnList = "tenant_id, observed_at")
       })
@Getter
@Setter
public class FieldReading extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReadingKind kind;

    @Column(name = "field_plot_id", nullable = false)
    private UUID fieldPlotId;

    @Column(name = "field_plot_name", length = 150)
    private String fieldPlotName;

    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt;

    /** Provider the reading came from, or the device code for a direct push. */
    @Column(length = 80)
    private String source;

    @Column(name = "device_code", length = 80)
    private String deviceCode;

    // ── WEATHER ──────────────────────────────────────────────────────────────
    @Column(name = "temperature_c", precision = 8, scale = 2)
    private BigDecimal temperatureC;

    @Column(name = "humidity_percent", precision = 8, scale = 2)
    private BigDecimal humidityPercent;

    @Column(name = "rainfall_mm", precision = 8, scale = 2)
    private BigDecimal rainfallMm;

    @Column(name = "wind_speed_kph", precision = 8, scale = 2)
    private BigDecimal windSpeedKph;

    @Column(name = "weather_summary", length = 100)
    private String weatherSummary;

    // ── SOIL ─────────────────────────────────────────────────────────────────
    @Column(name = "soil_moisture_percent", precision = 8, scale = 2)
    private BigDecimal soilMoisturePercent;

    @Column(name = "soil_temperature_c", precision = 8, scale = 2)
    private BigDecimal soilTemperatureC;

    @Column(name = "soil_ph", precision = 5, scale = 2)
    private BigDecimal soilPh;

    /** Electrical conductivity, dS/m — a salinity proxy. */
    @Column(name = "soil_ec", precision = 8, scale = 2)
    private BigDecimal soilEc;

    @Column(name = "nitrogen_ppm", precision = 10, scale = 2)
    private BigDecimal nitrogenPpm;

    @Column(name = "phosphorus_ppm", precision = 10, scale = 2)
    private BigDecimal phosphorusPpm;

    @Column(name = "potassium_ppm", precision = 10, scale = 2)
    private BigDecimal potassiumPpm;

    // ── SATELLITE ────────────────────────────────────────────────────────────
    /** Normalised difference vegetation index, -1 to 1. Higher is denser, healthier canopy. */
    @Column(precision = 6, scale = 3)
    private BigDecimal ndvi;

    /** Enhanced vegetation index — less prone to saturating on dense canopy than NDVI. */
    @Column(precision = 6, scale = 3)
    private BigDecimal evi;

    @Column(name = "cloud_cover_percent", precision = 6, scale = 2)
    private BigDecimal cloudCoverPercent;

    @Column(name = "scene_id", length = 120)
    private String sceneId;

    public enum ReadingKind {
        WEATHER, SOIL, SATELLITE
    }
}

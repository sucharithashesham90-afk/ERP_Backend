package com.erp.platform.modules.fieldiot.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** A sensor in a field. Devices push readings in; the dashboard reports which have gone quiet. */
@Entity(name = "IotDevice")
@Table(name = "iot_devices",
       indexes = {
           @Index(name = "idx_iotdevice_tenant", columnList = "tenant_id"),
           @Index(name = "idx_iotdevice_field", columnList = "tenant_id, field_plot_id"),
           @Index(name = "idx_iotdevice_code", columnList = "tenant_id, device_code")
       })
@Getter
@Setter
public class IotDevice extends TenantEntity {

    /** Identifier the physical device sends with each reading. */
    @Column(name = "device_code", nullable = false, length = 80)
    private String deviceCode;

    @Column(nullable = false, length = 150)
    private String name;

    /** SOIL_PROBE, WEATHER_STATION, RAIN_GAUGE, FLOW_METER … */
    @Column(name = "device_type", length = 40)
    private String deviceType;

    @Column(name = "field_plot_id")
    private UUID fieldPlotId;

    @Column(name = "field_plot_name", length = 150)
    private String fieldPlotName;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "battery_percent")
    private Integer batteryPercent;

    @Column(name = "signal_percent")
    private Integer signalPercent;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String notes;
}

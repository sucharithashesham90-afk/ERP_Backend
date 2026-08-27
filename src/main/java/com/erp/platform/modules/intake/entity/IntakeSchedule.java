package com.erp.platform.modules.intake.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "intake_schedules",
       indexes = {
           @Index(name = "idx_is_tenant", columnList = "tenant_id"),
           @Index(name = "idx_is_status", columnList = "tenant_id, status"),
           @Index(name = "idx_is_vendor", columnList = "tenant_id, vendor_id"),
           @Index(name = "idx_is_schedule_number", columnList = "tenant_id, schedule_number")
       })
@Getter
@Setter
public class IntakeSchedule extends TenantEntity {

    @Column(name = "schedule_number", nullable = false, length = 50)
    private String scheduleNumber;

    @Column(name = "vendor_id")
    private UUID vendorId;

    @Column(name = "vendor_name", length = 200)
    private String vendorName;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status = ScheduleStatus.DRAFT;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "driver_name", length = 200)
    private String driverName;

    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    @Column(name = "expected_quantity", precision = 18, scale = 4)
    private BigDecimal expectedQuantity = BigDecimal.ZERO;

    @Column(name = "actual_quantity", precision = 18, scale = 4)
    private BigDecimal actualQuantity = BigDecimal.ZERO;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(name = "location_name", length = 150)
    private String locationName;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntakeScheduleItem> items = new ArrayList<>();

    public enum ScheduleStatus {
        DRAFT, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}

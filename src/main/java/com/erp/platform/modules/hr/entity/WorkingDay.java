package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "working_days",
       indexes = {@Index(name = "idx_workday_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class WorkingDay extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String shiftName;

    @Column(length = 10)
    private String dayOfWeek; // MON, TUE, WED, THU, FRI, SAT, SUN

    @Column(name = "is_working")
    private boolean working = true;

    @Column(name = "work_hours")
    private Double workHours = 8.0;

    @Column(name = "start_time", length = 10)
    private String startTime; // HH:mm

    @Column(name = "end_time", length = 10)
    private String endTime; // HH:mm

    @Column(name = "break_minutes")
    private int breakMinutes = 60;

    @Column(nullable = false)
    private boolean active = true;
}

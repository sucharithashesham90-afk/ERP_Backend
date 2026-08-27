package com.erp.platform.modules.hr.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "attendance",
       indexes = {@Index(name = "idx_att_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Attendance extends TenantEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    private LocalDate date;

    @Column(name = "check_in")
    private LocalTime checkIn;

    @Column(name = "check_out")
    private LocalTime checkOut;

    @Column(length = 15)
    private String status;

    @Column(name = "working_hours")
    private Double workingHours = 0.0;

    @Column(length = 500)
    private String notes;
}

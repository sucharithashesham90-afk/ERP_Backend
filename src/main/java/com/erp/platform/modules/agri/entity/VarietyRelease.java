package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity(name = "AgriVarietyRelease")
@Table(name = "agri_variety_releases", indexes = {@Index(name = "idx_vr_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class VarietyRelease extends TenantEntity {

    @Column(name = "variety_code", length = 50, nullable = false)
    private String varietyCode;

    @Column(name = "variety_name", length = 200)
    private String varietyName;

    @Column(name = "crop_name", length = 100)
    private String cropName;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "notification_number", length = 100)
    private String notificationNumber;

    @Column(name = "notification_date")
    private LocalDate notificationDate;

    @Column(name = "certifying_body", length = 200)
    private String certifyingBody;

    @Column(name = "seed_class", length = 50)
    private String seedClass;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active")
    private boolean active = true;
}

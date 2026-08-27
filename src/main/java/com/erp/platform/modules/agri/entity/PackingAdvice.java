package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity(name = "AgriPackingAdvice")
@Table(name = "agri_packing_advices", indexes = {@Index(name = "idx_pa_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PackingAdvice extends TenantEntity {

    @Column(name = "advice_number", length = 50, nullable = false)
    private String adviceNumber;

    @Column(name = "advice_date")
    private LocalDate adviceDate;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "pack_size", length = 50)
    private String packSize;

    @Column(name = "total_bags")
    private Integer totalBags;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "job_card_number", length = 50)
    private String jobCardNumber;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "status", length = 20)
    private String status = "PENDING";
}

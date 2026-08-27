package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "physical_counts",
       indexes = {@Index(name = "idx_pc_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PhysicalCount extends TenantEntity {

    @Column(name = "count_number", nullable = false, length = 50)
    private String countNumber;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "warehouse_name", length = 200)
    private String warehouseName;

    @Column(name = "count_date")
    private LocalDate countDate;

    @Column(length = 20)
    private String status = "DRAFT";

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "physicalCount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhysicalCountItem> items = new ArrayList<>();
}

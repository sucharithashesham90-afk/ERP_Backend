package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "process_boms",
       indexes = {
           @Index(name = "idx_pbom_tenant", columnList = "tenant_id")
       })
@Getter
@Setter
public class ProcessBOM extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "processBom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProcessBOMItem> items = new ArrayList<>();
}

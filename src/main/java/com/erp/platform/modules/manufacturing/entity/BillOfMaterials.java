package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bill_of_materials",
       indexes = {@Index(name = "idx_bom_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class BillOfMaterials extends TenantEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String version = "1.0";

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BOMItem> items = new ArrayList<>();
}

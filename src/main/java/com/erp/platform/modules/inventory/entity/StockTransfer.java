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
@Table(name = "stock_transfers",
       indexes = {@Index(name = "idx_st_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class StockTransfer extends TenantEntity {

    @Column(name = "transfer_number", nullable = false, length = 50)
    private String transferNumber;

    @Column(name = "from_warehouse_id")
    private UUID fromWarehouseId;

    @Column(name = "from_warehouse_name", length = 200)
    private String fromWarehouseName;

    @Column(name = "to_warehouse_id")
    private UUID toWarehouseId;

    @Column(name = "to_warehouse_name", length = 200)
    private String toWarehouseName;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(length = 20)
    private String status = "DRAFT";

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockTransferItem> items = new ArrayList<>();
}

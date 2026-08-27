package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "AgriMaterialTransferNote")
@Table(name = "agri_material_transfer_notes", indexes = {@Index(name = "idx_material_transfer_note_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class MaterialTransferNote extends TenantEntity {

    @Column(name = "mtn_number", length = 100)
    private String mtnNumber;

    @Column(name = "mtn_date")
    private LocalDate mtnDate;

    @Column(name = "from_location", length = 200)
    private String fromLocation;

    @Column(name = "to_location", length = 200)
    private String toLocation;

    @Column(name = "purpose", length = 200)
    private String purpose;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "quantity_kgs", precision = 15, scale = 3)
    private BigDecimal quantityKgs;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "approved_by", length = 150)
    private String approvedBy;

    @Column(name = "remarks", length = 500)
    private String remarks;
}

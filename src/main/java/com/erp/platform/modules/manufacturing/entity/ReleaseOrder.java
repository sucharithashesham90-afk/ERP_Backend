package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Release Order (RO): raised from a Process Job's packed output line to release finished goods
 * from a godown/compartment. Created via the "Create RO" action on the Process Job screen.
 */
@Entity
@Table(name = "release_orders",
       indexes = {@Index(name = "idx_ro_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ReleaseOrder extends TenantEntity {

    @Column(name = "ro_number", length = 40)
    private String roNumber;

    @Column(name = "process_job_id")
    private UUID processJobId;

    @Column(name = "job_number", length = 60)
    private String jobNumber;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "output_lot_number", length = 60)
    private String outputLotNumber;

    @Column(precision = 18, scale = 3)
    private BigDecimal quantity;

    @Column(name = "godown_name", length = 200)
    private String godownName;

    @Column(name = "net_compartment_name", length = 200)
    private String netCompartmentName;

    @Column(name = "mrp_value", precision = 18, scale = 2)
    private BigDecimal mrpValue;

    @Column(length = 30)
    private String status = "CREATED";
}

package com.erp.platform.modules.agri.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "AgriTransferCertificate")
@Table(name = "agri_transfer_certificates", indexes = {
    @Index(name = "idx_transfer_cert_tenant", columnList = "tenant_id")
})
@Getter
@Setter
public class TransferCertificate extends TenantEntity {

    @Column(name = "tc_number", length = 50, nullable = false)
    private String tcNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "source_plant", length = 200)
    private String sourcePlant;

    @Column(name = "destination_plant", length = 200)
    private String destinationPlant;

    @Column(name = "number_of_bags")
    private Integer numberOfBags;

    @Column(name = "quantity_kgs", precision = 15, scale = 3)
    private BigDecimal quantityKgs;

    @Column(name = "input_type", length = 50)
    private String inputType;

    @Column(name = "bag_weight", precision = 10, scale = 3)
    private BigDecimal bagWeight;

    @Column(name = "intake_done")
    private Boolean intakeDone = false;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "status", length = 20)
    private String status = "ISSUED";
}

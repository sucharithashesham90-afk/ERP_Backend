package com.erp.platform.modules.shareholder.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "share_holders",
       indexes = {@Index(name = "idx_sh_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_sh_number", columnList = "shareholder_number")})
@Getter
@Setter
public class ShareHolder extends TenantEntity {

    @Column(name = "shareholder_number", length = 50, nullable = false)
    private String shareholderNumber;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "aadhar_number", length = 20)
    private String aadharNumber;

    @Column(name = "shares_held", precision = 15, scale = 0)
    private BigDecimal sharesHeld = BigDecimal.ZERO;

    @Column(name = "face_value_per_share", precision = 15, scale = 4)
    private BigDecimal faceValuePerShare;

    @Column(name = "date_of_allotment")
    private LocalDate dateOfAllotment;

    @Column(name = "nominee_name", length = 200)
    private String nomineeName;

    @Column(name = "nominee_relationship", length = 100)
    private String nomineeRelationship;

    /** ACTIVE / SUSPENDED / DECEASED / TRANSFERRED */
    @Column(name = "status", length = 30)
    private String status = "ACTIVE";

    @Column(name = "remarks", length = 500)
    private String remarks;
}

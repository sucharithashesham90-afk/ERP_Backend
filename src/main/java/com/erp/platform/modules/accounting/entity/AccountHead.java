package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Account Head definition (per the Accounting module spec).
 * An account head is a named posting head tied to an account group, categorised by the party
 * {@link #type} (STAFF / CUSTOMER / SUPPLIER) so the correct heads load on the corresponding screens.
 */
@Entity
@Table(name = "account_heads",
       indexes = {
           @Index(name = "idx_ah_tenant", columnList = "tenant_id"),
           @Index(name = "idx_ah_code",   columnList = "tenant_id,code")
       })
@Getter
@Setter
public class AccountHead extends TenantEntity {

    /** Account group this head belongs to (group definition code). */
    @Column(name = "group_code", length = 30)
    private String groupCode;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "account_head", nullable = false, length = 150)
    private String accountHead;

    @Column(length = 30)
    private String code;

    /** STAFF, CUSTOMER, SUPPLIER — drives where the head is loaded. */
    @Column(length = 20)
    private String type;

    @Column(nullable = false)
    private boolean active = true;
}

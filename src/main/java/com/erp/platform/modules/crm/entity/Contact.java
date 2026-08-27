package com.erp.platform.modules.crm.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "contacts",
       indexes = {@Index(name = "idx_contact_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Contact extends TenantEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String mobile;

    @Column(length = 100)
    private String designation;

    @Column(length = 100)
    private String department;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "is_primary")
    private boolean primary = false;

    @Column(length = 1000)
    private String notes;
}

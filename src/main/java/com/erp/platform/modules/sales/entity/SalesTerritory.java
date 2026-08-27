package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sales_territories",
       indexes = {
           @Index(name = "idx_territory_tenant", columnList = "tenant_id"),
           @Index(name = "idx_territory_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class SalesTerritory extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String region;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country = "India";

    // ---- Sales Area definition (per Sales module spec) ----

    /** The employee (from the Sales department) this sales area is assigned to. Was previously a
     *  free-text role name — changed to a real Employee reference since "assigned to" means a
     *  specific person, not a role, and Sales Area Levels needs the same person concept to route
     *  leave/reporting-manager style approvals correctly. */
    @Column(name = "assigned_to_employee_id")
    private java.util.UUID assignedToEmployeeId;

    @Column(name = "assigned_to_employee_name", length = 200)
    private String assignedToEmployeeName;

    /** Home location (from the locations screen). */
    @Column(length = 100)
    private String location;

    /** Currency code (from the currency screen). */
    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    /** Export sales area flag. */
    @Column(name = "is_export", nullable = false, columnDefinition = "boolean not null default false")
    private boolean export = false;

    @Column(nullable = false)
    private boolean active = true;
}

package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity(name = "SalesCustomerComplaint")
@Table(name = "sales_customer_complaints", indexes = {
    @Index(name = "idx_customer_complaint_tenant", columnList = "tenant_id")
})
@Getter
@Setter
public class CustomerComplaint extends TenantEntity {

    @Column(name = "complaint_number", length = 50, nullable = false)
    private String complaintNumber;

    @Column(name = "complaint_date")
    private LocalDate complaintDate;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "complaint_type", length = 100)
    private String complaintType;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "assigned_to", length = 200)
    private String assignedTo;

    @Column(name = "resolution_date")
    private LocalDate resolutionDate;

    @Column(name = "resolution", length = 2000)
    private String resolution;

    @Column(name = "status", length = 20)
    private String status = "OPEN";
}

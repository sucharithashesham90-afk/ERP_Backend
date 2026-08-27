package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.erp.platform.modules.master.entity.Vendor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_orders",
       indexes = {
           @Index(name = "idx_svcord_tenant", columnList = "tenant_id"),
           @Index(name = "idx_svcord_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class ServiceOrder extends TenantEntity {

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vendor vendor;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_completion_date")
    private LocalDate expectedCompletionDate;

    @Column(length = 1000)
    private String description;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ServiceOrderStatus status = ServiceOrderStatus.DRAFT;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOrderItem> items = new ArrayList<>();

    public enum ServiceOrderStatus {
        DRAFT, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}

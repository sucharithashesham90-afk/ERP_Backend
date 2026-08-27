package com.erp.platform.modules.purchase.entity;
import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
@Entity @Table(name="purchase_work_orders",indexes={@Index(name="idx_pwo_tenant",columnList="tenant_id")})
@Getter @Setter
public class PurchaseWorkOrder extends TenantEntity {
    @Column(name="order_number",length=50) private String orderNumber;
    @Column(nullable=false,length=200) private String title;
    @Column(length=1000) private String description;
    @Column(name="vendor_id") private UUID vendorId;
    @Column(name="vendor_name",length=200) private String vendorName;
    @Column(name="scheduled_date") private LocalDate scheduledDate;
    @Column(name="completed_date") private LocalDate completedDate;
    @Column(name="estimated_cost",precision=18,scale=2) private BigDecimal estimatedCost=BigDecimal.ZERO;
    @Column(name="actual_cost",precision=18,scale=2) private BigDecimal actualCost=BigDecimal.ZERO;
    @Column(length=30) private String status="DRAFT";
    @Column(length=20) private String priority="MEDIUM";
    private boolean active=true;
}

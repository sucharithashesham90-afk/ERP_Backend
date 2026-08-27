package com.erp.platform.modules.sales.entity;
import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
@Entity @Table(name="customer_discounts",indexes={@Index(name="idx_cdisc_tenant",columnList="tenant_id")})
@Getter @Setter
public class CustomerDiscount extends TenantEntity {
    @Column(name="customer_id") private UUID customerId;
    @Column(name="customer_name",length=200) private String customerName;
    @Column(name="product_category",length=100) private String productCategory;
    @Column(name="discount_pct",precision=8,scale=4) private BigDecimal discountPct=BigDecimal.ZERO;
    @Column(name="valid_from") private LocalDate validFrom;
    @Column(name="valid_to") private LocalDate validTo;
    private boolean active=true;
}

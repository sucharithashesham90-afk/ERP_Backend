package com.erp.platform.modules.accounting.entity;
import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="shareholders",indexes={@Index(name="idx_sh_tenant",columnList="tenant_id")})
@Getter @Setter
public class Shareholder extends TenantEntity {
    @Column(nullable=false,length=200) private String name;
    @Column(length=100) private String email;
    @Column(length=20) private String phone;
    @Column(name="pan_number",length=50) private String panNumber;
    @Column(name="folio_number",length=50) private String folioNumber;
    @Column(name="shares_held") private Long sharesHeld;
    @Column(name="holding_percentage",precision=8,scale=4) private BigDecimal holdingPercentage=BigDecimal.ZERO;
    @Column(name="allotment_date") private LocalDate allotmentDate;
    @Column(length=500) private String address;
    private boolean active=true;
}

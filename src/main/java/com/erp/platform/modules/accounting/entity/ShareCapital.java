package com.erp.platform.modules.accounting.entity;
import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="share_capital",indexes={@Index(name="idx_sc_tenant",columnList="tenant_id")})
@Getter @Setter
public class ShareCapital extends TenantEntity {
    @Column(name="share_series",nullable=false,length=100) private String shareSeries;
    @Column(name="share_type",length=50) private String shareType;
    @Column(name="authorized_shares") private Long authorizedShares;
    @Column(name="issued_shares") private Long issuedShares;
    @Column(name="paid_up_shares") private Long paidUpShares;
    @Column(name="face_value",precision=15,scale=2) private BigDecimal faceValue=BigDecimal.ZERO;
    @Column(name="paid_up_value",precision=15,scale=2) private BigDecimal paidUpValue=BigDecimal.ZERO;
    @Column(name="issue_date") private LocalDate issueDate;
    @Column(length=500) private String remarks;
    private boolean active=true;
}

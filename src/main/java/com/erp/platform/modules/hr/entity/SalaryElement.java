package com.erp.platform.modules.hr.entity;
import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.math.BigDecimal;
@Entity @Table(name="salary_elements",indexes={@Index(name="idx_se_tenant",columnList="tenant_id")})
@Getter @Setter
public class SalaryElement extends TenantEntity {
    @Column(nullable=false,length=100) private String name;
    @Column(length=30) private String code;
    @Column(name="element_type",length=20) private String elementType;
    @Column(name="calculation_type",length=20) private String calculationType;
    @Column(precision=18,scale=2) private BigDecimal amount=BigDecimal.ZERO;
    @Column(precision=8,scale=4) private BigDecimal percentage=BigDecimal.ZERO;
    /** What a percentage element is a percentage of — BASIC, GROSS, CTC. Null for a fixed amount. */
    @Column(name="percentage_base",length=30) private String percentageBase;
    @Column(length=500) private String formula;
    private boolean taxable=false;
    @Column(length=500) private String description;
    private boolean active=true;
}

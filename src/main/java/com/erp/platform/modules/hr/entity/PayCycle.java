package com.erp.platform.modules.hr.entity;
import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Entity @Table(name="pay_cycles",indexes={@Index(name="idx_pc_tenant",columnList="tenant_id")})
@Getter @Setter
public class PayCycle extends TenantEntity {
    @Column(nullable=false,length=100) private String name;
    @Column(length=30) private String code;
    @Column(length=20) private String frequency;
    @Column(name="start_day_of_month") private Integer startDayOfMonth=1;
    @Column(length=500) private String description;
    private boolean active=true;
}

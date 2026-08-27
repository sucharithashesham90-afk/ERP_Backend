package com.erp.platform.modules.master.entity;
import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Entity @Table(name="uom_categories",indexes={@Index(name="idx_uomcat_tenant",columnList="tenant_id")})
@Getter @Setter
public class UomCategory extends TenantEntity {
    @Column(nullable=false,length=100) private String name;
    @Column(length=30) private String code;
    @Column(length=500) private String description;
    private boolean active=true;
}

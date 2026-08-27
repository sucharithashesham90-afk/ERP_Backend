package com.erp.platform.modules.master.entity;
import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "currencies", indexes = {@Index(name = "idx_cur_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class Currency extends TenantEntity {
    @Column(name = "currency_code", nullable = false, length = 10) private String code;
    @Column(name = "currency_name", nullable = false, length = 100) private String name;
    @Column(length = 10) private String symbol;
    @Column(name = "decimal_places") private int decimalPlaces = 2;
    @Column(name = "exchange_rate", precision = 18, scale = 6) private BigDecimal exchangeRate = BigDecimal.ONE;
    @Column(name = "is_base_currency") private boolean baseCurrency = false;
    @Column(length = 500) private String description;
    private boolean active = true;
}

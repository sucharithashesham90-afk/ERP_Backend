package com.erp.platform.modules.master.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "geo_countries",
       indexes = {
           @Index(name = "idx_geocountry_tenant", columnList = "tenant_id"),
           @Index(name = "idx_geocountry_iso2",   columnList = "tenant_id, iso_code2")
       })
@Getter
@Setter
public class GeoCountry extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "iso_code2", length = 3)
    private String isoCode2;

    @Column(name = "iso_code3", length = 4)
    private String isoCode3;

    @Column(name = "dialing_code", length = 10)
    private String dialingCode;

    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    @Column(name = "currency_symbol", length = 10)
    private String currencySymbol;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String notes;
}

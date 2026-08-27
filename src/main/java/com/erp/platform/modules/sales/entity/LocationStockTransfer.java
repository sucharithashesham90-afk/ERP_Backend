package com.erp.platform.modules.sales.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Location-to-location stock transfer (per the Sales module spec). A lot-based movement: on posting,
 * stock leaves the from-location's lot and arrives at the to-location. It is an internal inventory
 * move (no customer ledger involved).
 */
@Entity
@Table(name = "location_stock_transfers",
       indexes = {@Index(name = "idx_lst_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class LocationStockTransfer extends TenantEntity {

    @Column(name = "transfer_number", length = 40)
    private String transferNumber;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(name = "from_location", length = 200)
    private String fromLocation;

    @Column(name = "to_location", length = 200)
    private String toLocation;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "freight_total", precision = 18, scale = 2)
    private BigDecimal freightTotal;

    @Column(name = "freight_paid", precision = 18, scale = 2)
    private BigDecimal freightPaid;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 30)
    private String status = "DRAFT";

    @Column(nullable = false)
    private boolean posted = false;

    @Column(name = "freight_to_pay", precision = 18, scale = 2)
    private BigDecimal freightToPay;

    // ── From-location address ──
    @Column(name = "from_address1", length = 300) private String fromAddress1;
    @Column(name = "from_address2", length = 300) private String fromAddress2;
    @Column(name = "from_state", length = 120)    private String fromState;
    @Column(name = "from_district", length = 120) private String fromDistrict;
    @Column(name = "from_city", length = 120)     private String fromCity;
    @Column(name = "from_zip", length = 20)       private String fromZip;
    @Column(name = "from_phone", length = 40)     private String fromPhone;

    // ── To-location address ──
    @Column(name = "to_address1", length = 300) private String toAddress1;
    @Column(name = "to_address2", length = 300) private String toAddress2;
    @Column(name = "to_state", length = 120)    private String toState;
    @Column(name = "to_district", length = 120) private String toDistrict;
    @Column(name = "to_city", length = 120)     private String toCity;
    @Column(name = "to_zip", length = 20)       private String toZip;
    @Column(name = "to_phone", length = 40)     private String toPhone;

    // ── Line items (reuses the dealer transfer line shape) ──
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "location_stock_transfer_items", joinColumns = @JoinColumn(name = "transfer_id"))
    private List<CustomerTransferLine> items = new ArrayList<>();
}

package com.erp.platform.modules.sales.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One line of a dealer-to-dealer stock transfer: a product/lot with its from-side
 * and to-side pricing and the resulting from/to customer amounts.
 */
@Embeddable
@Getter
@Setter
public class CustomerTransferLine {

    @Column(name = "crop_group", length = 150)
    private String cropGroup;
    @Column(name = "crop", length = 150)
    private String crop;
    @Column(name = "variety", length = 150)
    private String variety;
    @Column(name = "crop_variety", length = 300)
    private String cropVariety;

    @Column(name = "product_id", length = 100)
    private String productId;
    @Column(name = "product_name", length = 200)
    private String productName;
    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "packs", precision = 15, scale = 3)
    private BigDecimal packs;
    @Column(name = "packs_damaged", precision = 15, scale = 3)
    private BigDecimal packsDamaged;

    @Column(name = "from_unit_price", precision = 18, scale = 2)
    private BigDecimal fromUnitPrice;
    @Column(name = "from_discount", precision = 18, scale = 2)
    private BigDecimal fromDiscount;
    @Column(name = "from_st_cst", precision = 18, scale = 2)
    private BigDecimal fromStCst;
    @Column(name = "from_cust_price", precision = 18, scale = 2)
    private BigDecimal fromCustPrice;

    @Column(name = "to_unit_price", precision = 18, scale = 2)
    private BigDecimal toUnitPrice;
    @Column(name = "to_discount", precision = 18, scale = 2)
    private BigDecimal toDiscount;
    @Column(name = "to_st_cst", precision = 18, scale = 2)
    private BigDecimal toStCst;
    @Column(name = "to_cust_price", precision = 18, scale = 2)
    private BigDecimal toCustPrice;

    @Column(name = "from_customer_amount", precision = 18, scale = 2)
    private BigDecimal fromCustomerAmount;
    @Column(name = "to_customer_amount", precision = 18, scale = 2)
    private BigDecimal toCustomerAmount;
}

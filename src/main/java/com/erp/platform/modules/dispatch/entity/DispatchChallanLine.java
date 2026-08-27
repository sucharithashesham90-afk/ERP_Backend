package com.erp.platform.modules.dispatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A single line on a dispatch challan (one product / lot / quantity).
 * Stored as an element collection under the parent {@link DispatchChallan}.
 */
@Embeddable
@Getter
@Setter
public class DispatchChallanLine {

    @Column(name = "product_id", length = 100)
    private String productId;

    @Column(name = "crop_group", length = 150)
    private String cropGroup;
    @Column(name = "crop", length = 150)
    private String crop;
    @Column(name = "variety", length = 150)
    private String variety;
    @Column(name = "crop_variety", length = 300)
    private String cropVariety;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "packing", length = 200)
    private String packing;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "quantity", precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "rate", precision = 18, scale = 2)
    private BigDecimal rate;

    @Column(name = "line_value", precision = 18, scale = 2)
    private BigDecimal value;
}

package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.Product.ProductType;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateProductRequest {

    @Size(max = 200)
    private String name;

    @Size(max = 50)
    private String code;

    @Size(max = 100)
    private String sku;

    @Size(max = 100)
    private String barcode;

    @Size(max = 1000)
    private String description;

    private UUID categoryId;

    private UUID materialGroupId;

    private UUID varietyId;

    private String varietyName;

    private ProductType productType;

    @Size(max = 20)
    private String unit;

    @PositiveOrZero
    private BigDecimal purchasePrice;

    @PositiveOrZero
    private BigDecimal salePrice;

    @PositiveOrZero
    private BigDecimal mrp;

    private UUID taxRateId;

    @PositiveOrZero
    private BigDecimal reorderLevel;

    private Boolean trackInventory;

    private Boolean active;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 1000)
    private String notes;
}

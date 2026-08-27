package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.Product.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 30)
    private String code;

    @Size(max = 50)
    private String sku;

    @Size(max = 50)
    private String barcode;

    @Size(max = 1000)
    private String description;

    private UUID categoryId;

    private UUID materialGroupId;

    private UUID varietyId;

    private String varietyName;

    private ProductType productType = ProductType.GOODS;

    @Size(max = 20)
    private String unit = "PCS";

    @PositiveOrZero
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @PositiveOrZero
    private BigDecimal salePrice = BigDecimal.ZERO;

    @PositiveOrZero
    private BigDecimal mrp = BigDecimal.ZERO;

    private UUID taxRateId;

    @PositiveOrZero
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    private boolean trackInventory = true;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 1000)
    private String notes;
}

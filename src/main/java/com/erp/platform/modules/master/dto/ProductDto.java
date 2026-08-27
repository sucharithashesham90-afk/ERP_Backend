package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.Product.ProductType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductDto {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String code;
    private String sku;
    private String barcode;
    private String description;
    private UUID categoryId;
    private String categoryName;
    private UUID materialGroupId;
    private String materialGroupName;
    private UUID varietyId;
    private String varietyName;
    private ProductType productType;
    private String unit;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private BigDecimal mrp;
    private UUID taxRateId;
    private String taxRateName;
    private BigDecimal reorderLevel;
    private BigDecimal currentStock;
    private boolean trackInventory;
    private boolean active;
    private LocalDateTime createdAt;
}

package com.erp.platform.modules.master.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProductBomLineDto {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productCode;
    private UUID brandId;
    private String brandName;
    private UUID materialGroupId;
    private String materialGroupName;
    private UUID materialItemId;
    private String materialItemName;
    private String bomType;
    private BigDecimal qtyPerPack;
    private String unit;
    private BigDecimal mrp;
    private String notes;
    private int sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

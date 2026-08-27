package com.erp.platform.modules.master.dto;

import com.erp.platform.modules.master.entity.Tax.TaxType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaxDto {
    private UUID id;
    private UUID tenantId;
    private String name;
    private TaxType taxType;
    private String type;
    private BigDecimal rate;
    private boolean compound;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
}

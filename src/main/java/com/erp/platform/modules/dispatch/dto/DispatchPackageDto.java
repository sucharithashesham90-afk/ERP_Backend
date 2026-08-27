package com.erp.platform.modules.dispatch.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DispatchPackageDto {

    private UUID id;
    private UUID dispatchId;
    private String packageNumber;
    private String packageType;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal grossWeight;
    private BigDecimal netWeight;
    private String sealNumber;
}

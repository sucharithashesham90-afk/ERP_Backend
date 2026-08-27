package com.erp.platform.modules.inventory.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class WarehouseDto {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String code;
    private String address;
    private String city;
    private String contactPerson;
    private String phone;
    private boolean active;
    private boolean isDefault;
    private String location;
    private LocalDateTime createdAt;
}

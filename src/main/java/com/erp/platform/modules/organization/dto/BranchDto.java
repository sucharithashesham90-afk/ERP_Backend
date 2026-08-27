package com.erp.platform.modules.organization.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BranchDto {
    private UUID id;
    private UUID tenantId;
    private UUID companyId;
    private String name;
    private String code;
    private String address;
    private String city;
    private String state;
    private String phone;
    private String email;
    private boolean isHeadOffice;
    private boolean active;
    private LocalDateTime createdAt;
}

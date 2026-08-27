package com.erp.platform.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateWarehouseRequest {

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 30)
    private String code;

    @Size(max = 500)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 150)
    private String contactPerson;

    @Size(max = 20)
    private String phone;

    private boolean isDefault = false;

    @Size(max = 150)
    private String location;
}

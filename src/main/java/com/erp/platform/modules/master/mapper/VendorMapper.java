package com.erp.platform.modules.master.mapper;

import com.erp.platform.modules.master.dto.CreateVendorRequest;
import com.erp.platform.modules.master.dto.UpdateVendorRequest;
import com.erp.platform.modules.master.dto.VendorDto;
import com.erp.platform.modules.master.entity.Vendor;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VendorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "outstandingBalance", ignore = true)
    @Mapping(target = "active", ignore = true)
    Vendor toEntity(CreateVendorRequest request);

    VendorDto toDto(Vendor vendor);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "outstandingBalance", ignore = true)
    void updateEntity(UpdateVendorRequest request, @MappingTarget Vendor vendor);
}

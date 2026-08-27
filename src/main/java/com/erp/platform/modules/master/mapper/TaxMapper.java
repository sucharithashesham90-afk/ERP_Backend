package com.erp.platform.modules.master.mapper;

import com.erp.platform.modules.master.dto.CreateTaxRequest;
import com.erp.platform.modules.master.dto.TaxDto;
import com.erp.platform.modules.master.entity.Tax;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaxMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Tax toEntity(CreateTaxRequest request);

    TaxDto toDto(Tax tax);
}

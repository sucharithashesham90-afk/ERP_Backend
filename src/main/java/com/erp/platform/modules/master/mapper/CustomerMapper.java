package com.erp.platform.modules.master.mapper;

import com.erp.platform.modules.master.dto.CreateCustomerRequest;
import com.erp.platform.modules.master.dto.CustomerDto;
import com.erp.platform.modules.master.dto.UpdateCustomerRequest;
import com.erp.platform.modules.master.entity.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "outstandingBalance", ignore = true)
    @Mapping(target = "active", ignore = true)
    Customer toEntity(CreateCustomerRequest request);

    CustomerDto toDto(Customer customer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "outstandingBalance", ignore = true)
    void updateEntity(UpdateCustomerRequest request, @MappingTarget Customer customer);
}

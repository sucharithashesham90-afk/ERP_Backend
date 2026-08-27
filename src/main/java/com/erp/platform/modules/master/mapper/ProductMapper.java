package com.erp.platform.modules.master.mapper;

import com.erp.platform.modules.master.dto.CreateProductRequest;
import com.erp.platform.modules.master.dto.ProductDto;
import com.erp.platform.modules.master.dto.UpdateProductRequest;
import com.erp.platform.modules.master.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "currentStock", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "materialGroupName", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "taxRateId", source = "taxRate.id")
    @Mapping(target = "taxRateName", source = "taxRate.name")
    ProductDto toDto(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "currentStock", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "taxRate", ignore = true)
    @Mapping(target = "materialGroupName", ignore = true)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product product);
}

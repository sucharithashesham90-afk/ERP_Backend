package com.erp.platform.modules.master.mapper;

import com.erp.platform.modules.master.dto.CreateProductCategoryRequest;
import com.erp.platform.modules.master.dto.ProductCategoryDto;
import com.erp.platform.modules.master.entity.ProductCategory;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductCategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "parent", ignore = true)
    ProductCategory toEntity(CreateProductCategoryRequest request);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    ProductCategoryDto toDto(ProductCategory category);
}

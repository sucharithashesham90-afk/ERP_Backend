package com.erp.platform.modules.master.mapper;

import com.erp.platform.modules.master.dto.CreateProductCategoryRequest;
import com.erp.platform.modules.master.dto.ProductCategoryDto;
import com.erp.platform.modules.master.entity.ProductCategory;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-27T10:05:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ProductCategoryMapperImpl implements ProductCategoryMapper {

    @Override
    public ProductCategory toEntity(CreateProductCategoryRequest request) {
        if ( request == null ) {
            return null;
        }

        ProductCategory productCategory = new ProductCategory();

        productCategory.setCode( request.getCode() );
        productCategory.setDescription( request.getDescription() );
        productCategory.setName( request.getName() );

        return productCategory;
    }

    @Override
    public ProductCategoryDto toDto(ProductCategory category) {
        if ( category == null ) {
            return null;
        }

        ProductCategoryDto productCategoryDto = new ProductCategoryDto();

        productCategoryDto.setParentId( categoryParentId( category ) );
        productCategoryDto.setParentName( categoryParentName( category ) );
        productCategoryDto.setActive( category.isActive() );
        productCategoryDto.setCode( category.getCode() );
        productCategoryDto.setCreatedAt( category.getCreatedAt() );
        productCategoryDto.setDescription( category.getDescription() );
        productCategoryDto.setId( category.getId() );
        productCategoryDto.setName( category.getName() );
        productCategoryDto.setTenantId( category.getTenantId() );

        return productCategoryDto;
    }

    private UUID categoryParentId(ProductCategory productCategory) {
        if ( productCategory == null ) {
            return null;
        }
        ProductCategory parent = productCategory.getParent();
        if ( parent == null ) {
            return null;
        }
        UUID id = parent.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String categoryParentName(ProductCategory productCategory) {
        if ( productCategory == null ) {
            return null;
        }
        ProductCategory parent = productCategory.getParent();
        if ( parent == null ) {
            return null;
        }
        String name = parent.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}

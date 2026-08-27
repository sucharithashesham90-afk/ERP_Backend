package com.erp.platform.modules.master.mapper;

import com.erp.platform.modules.master.dto.CreateProductRequest;
import com.erp.platform.modules.master.dto.ProductDto;
import com.erp.platform.modules.master.dto.UpdateProductRequest;
import com.erp.platform.modules.master.entity.Product;
import com.erp.platform.modules.master.entity.ProductCategory;
import com.erp.platform.modules.master.entity.Tax;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-27T10:05:35+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toEntity(CreateProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product product = new Product();

        product.setBarcode( request.getBarcode() );
        product.setCode( request.getCode() );
        product.setDescription( request.getDescription() );
        product.setImageUrl( request.getImageUrl() );
        product.setMaterialGroupId( request.getMaterialGroupId() );
        product.setMrp( request.getMrp() );
        product.setName( request.getName() );
        product.setNotes( request.getNotes() );
        product.setProductType( request.getProductType() );
        product.setPurchasePrice( request.getPurchasePrice() );
        product.setReorderLevel( request.getReorderLevel() );
        product.setSalePrice( request.getSalePrice() );
        product.setSku( request.getSku() );
        product.setTrackInventory( request.isTrackInventory() );
        product.setUnit( request.getUnit() );
        product.setVarietyId( request.getVarietyId() );
        product.setVarietyName( request.getVarietyName() );

        return product;
    }

    @Override
    public ProductDto toDto(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductDto productDto = new ProductDto();

        productDto.setCategoryId( productCategoryId( product ) );
        productDto.setCategoryName( productCategoryName( product ) );
        productDto.setTaxRateId( productTaxRateId( product ) );
        productDto.setTaxRateName( productTaxRateName( product ) );
        productDto.setActive( product.isActive() );
        productDto.setBarcode( product.getBarcode() );
        productDto.setCode( product.getCode() );
        productDto.setCreatedAt( product.getCreatedAt() );
        productDto.setCurrentStock( product.getCurrentStock() );
        productDto.setDescription( product.getDescription() );
        productDto.setId( product.getId() );
        productDto.setMaterialGroupId( product.getMaterialGroupId() );
        productDto.setMaterialGroupName( product.getMaterialGroupName() );
        productDto.setMrp( product.getMrp() );
        productDto.setName( product.getName() );
        productDto.setProductType( product.getProductType() );
        productDto.setPurchasePrice( product.getPurchasePrice() );
        productDto.setReorderLevel( product.getReorderLevel() );
        productDto.setSalePrice( product.getSalePrice() );
        productDto.setSku( product.getSku() );
        productDto.setTenantId( product.getTenantId() );
        productDto.setTrackInventory( product.isTrackInventory() );
        productDto.setUnit( product.getUnit() );
        productDto.setVarietyId( product.getVarietyId() );
        productDto.setVarietyName( product.getVarietyName() );

        return productDto;
    }

    @Override
    public void updateEntity(UpdateProductRequest request, Product product) {
        if ( request == null ) {
            return;
        }

        if ( request.getActive() != null ) {
            product.setActive( request.getActive() );
        }
        if ( request.getBarcode() != null ) {
            product.setBarcode( request.getBarcode() );
        }
        if ( request.getCode() != null ) {
            product.setCode( request.getCode() );
        }
        if ( request.getDescription() != null ) {
            product.setDescription( request.getDescription() );
        }
        if ( request.getImageUrl() != null ) {
            product.setImageUrl( request.getImageUrl() );
        }
        if ( request.getMaterialGroupId() != null ) {
            product.setMaterialGroupId( request.getMaterialGroupId() );
        }
        if ( request.getMrp() != null ) {
            product.setMrp( request.getMrp() );
        }
        if ( request.getName() != null ) {
            product.setName( request.getName() );
        }
        if ( request.getNotes() != null ) {
            product.setNotes( request.getNotes() );
        }
        if ( request.getProductType() != null ) {
            product.setProductType( request.getProductType() );
        }
        if ( request.getPurchasePrice() != null ) {
            product.setPurchasePrice( request.getPurchasePrice() );
        }
        if ( request.getReorderLevel() != null ) {
            product.setReorderLevel( request.getReorderLevel() );
        }
        if ( request.getSalePrice() != null ) {
            product.setSalePrice( request.getSalePrice() );
        }
        if ( request.getSku() != null ) {
            product.setSku( request.getSku() );
        }
        if ( request.getTrackInventory() != null ) {
            product.setTrackInventory( request.getTrackInventory() );
        }
        if ( request.getUnit() != null ) {
            product.setUnit( request.getUnit() );
        }
        if ( request.getVarietyId() != null ) {
            product.setVarietyId( request.getVarietyId() );
        }
        if ( request.getVarietyName() != null ) {
            product.setVarietyName( request.getVarietyName() );
        }
    }

    private UUID productCategoryId(Product product) {
        if ( product == null ) {
            return null;
        }
        ProductCategory category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        UUID id = category.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String productCategoryName(Product product) {
        if ( product == null ) {
            return null;
        }
        ProductCategory category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private UUID productTaxRateId(Product product) {
        if ( product == null ) {
            return null;
        }
        Tax taxRate = product.getTaxRate();
        if ( taxRate == null ) {
            return null;
        }
        UUID id = taxRate.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String productTaxRateName(Product product) {
        if ( product == null ) {
            return null;
        }
        Tax taxRate = product.getTaxRate();
        if ( taxRate == null ) {
            return null;
        }
        String name = taxRate.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}

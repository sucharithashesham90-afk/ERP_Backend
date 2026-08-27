package com.erp.platform.modules.master.mapper;

import com.erp.platform.modules.master.dto.CreateTaxRequest;
import com.erp.platform.modules.master.dto.TaxDto;
import com.erp.platform.modules.master.entity.Tax;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-27T10:05:34+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class TaxMapperImpl implements TaxMapper {

    @Override
    public Tax toEntity(CreateTaxRequest request) {
        if ( request == null ) {
            return null;
        }

        Tax tax = new Tax();

        tax.setCompound( request.isCompound() );
        tax.setDescription( request.getDescription() );
        tax.setName( request.getName() );
        tax.setRate( request.getRate() );
        tax.setTaxType( request.getTaxType() );
        tax.setType( request.getType() );

        return tax;
    }

    @Override
    public TaxDto toDto(Tax tax) {
        if ( tax == null ) {
            return null;
        }

        TaxDto taxDto = new TaxDto();

        taxDto.setActive( tax.isActive() );
        taxDto.setCompound( tax.isCompound() );
        taxDto.setCreatedAt( tax.getCreatedAt() );
        taxDto.setDescription( tax.getDescription() );
        taxDto.setId( tax.getId() );
        taxDto.setName( tax.getName() );
        taxDto.setRate( tax.getRate() );
        taxDto.setTaxType( tax.getTaxType() );
        taxDto.setTenantId( tax.getTenantId() );
        taxDto.setType( tax.getType() );

        return taxDto;
    }
}

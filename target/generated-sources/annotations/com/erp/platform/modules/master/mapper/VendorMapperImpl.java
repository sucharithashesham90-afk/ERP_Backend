package com.erp.platform.modules.master.mapper;

import com.erp.platform.modules.master.dto.CreateVendorRequest;
import com.erp.platform.modules.master.dto.UpdateVendorRequest;
import com.erp.platform.modules.master.dto.VendorDto;
import com.erp.platform.modules.master.entity.Vendor;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-27T10:05:34+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class VendorMapperImpl implements VendorMapper {

    @Override
    public Vendor toEntity(CreateVendorRequest request) {
        if ( request == null ) {
            return null;
        }

        Vendor vendor = new Vendor();

        vendor.setAddress( request.getAddress() );
        vendor.setAddress2( request.getAddress2() );
        vendor.setBankAccount( request.getBankAccount() );
        vendor.setBankName( request.getBankName() );
        vendor.setBankRoutingCode( request.getBankRoutingCode() );
        vendor.setCategory( request.getCategory() );
        vendor.setCity( request.getCity() );
        vendor.setCode( request.getCode() );
        vendor.setContactPerson( request.getContactPerson() );
        vendor.setCountry( request.getCountry() );
        vendor.setDistrict( request.getDistrict() );
        vendor.setEmail( request.getEmail() );
        vendor.setFax( request.getFax() );
        vendor.setMobile( request.getMobile() );
        vendor.setName( request.getName() );
        vendor.setNotes( request.getNotes() );
        vendor.setPanNumber( request.getPanNumber() );
        vendor.setPaymentTermsDays( request.getPaymentTermsDays() );
        vendor.setPhone( request.getPhone() );
        vendor.setPhoto( request.getPhoto() );
        vendor.setPostalCode( request.getPostalCode() );
        vendor.setState( request.getState() );
        vendor.setTaxNumber( request.getTaxNumber() );

        return vendor;
    }

    @Override
    public VendorDto toDto(Vendor vendor) {
        if ( vendor == null ) {
            return null;
        }

        VendorDto vendorDto = new VendorDto();

        vendorDto.setActive( vendor.isActive() );
        vendorDto.setAddress( vendor.getAddress() );
        vendorDto.setAddress2( vendor.getAddress2() );
        vendorDto.setBankAccount( vendor.getBankAccount() );
        vendorDto.setBankName( vendor.getBankName() );
        vendorDto.setBankRoutingCode( vendor.getBankRoutingCode() );
        vendorDto.setCategory( vendor.getCategory() );
        vendorDto.setCity( vendor.getCity() );
        vendorDto.setCode( vendor.getCode() );
        vendorDto.setContactPerson( vendor.getContactPerson() );
        vendorDto.setCountry( vendor.getCountry() );
        vendorDto.setCreatedAt( vendor.getCreatedAt() );
        vendorDto.setDistrict( vendor.getDistrict() );
        vendorDto.setEmail( vendor.getEmail() );
        vendorDto.setFax( vendor.getFax() );
        vendorDto.setId( vendor.getId() );
        vendorDto.setMobile( vendor.getMobile() );
        vendorDto.setName( vendor.getName() );
        vendorDto.setNotes( vendor.getNotes() );
        vendorDto.setOutstandingBalance( vendor.getOutstandingBalance() );
        vendorDto.setPanNumber( vendor.getPanNumber() );
        vendorDto.setPaymentTermsDays( vendor.getPaymentTermsDays() );
        vendorDto.setPhone( vendor.getPhone() );
        vendorDto.setPhoto( vendor.getPhoto() );
        vendorDto.setPostalCode( vendor.getPostalCode() );
        vendorDto.setState( vendor.getState() );
        vendorDto.setTaxNumber( vendor.getTaxNumber() );
        vendorDto.setTenantId( vendor.getTenantId() );

        return vendorDto;
    }

    @Override
    public void updateEntity(UpdateVendorRequest request, Vendor vendor) {
        if ( request == null ) {
            return;
        }

        if ( request.getActive() != null ) {
            vendor.setActive( request.getActive() );
        }
        if ( request.getAddress() != null ) {
            vendor.setAddress( request.getAddress() );
        }
        if ( request.getAddress2() != null ) {
            vendor.setAddress2( request.getAddress2() );
        }
        if ( request.getBankAccount() != null ) {
            vendor.setBankAccount( request.getBankAccount() );
        }
        if ( request.getBankName() != null ) {
            vendor.setBankName( request.getBankName() );
        }
        if ( request.getBankRoutingCode() != null ) {
            vendor.setBankRoutingCode( request.getBankRoutingCode() );
        }
        if ( request.getCategory() != null ) {
            vendor.setCategory( request.getCategory() );
        }
        if ( request.getCity() != null ) {
            vendor.setCity( request.getCity() );
        }
        if ( request.getCode() != null ) {
            vendor.setCode( request.getCode() );
        }
        if ( request.getContactPerson() != null ) {
            vendor.setContactPerson( request.getContactPerson() );
        }
        if ( request.getCountry() != null ) {
            vendor.setCountry( request.getCountry() );
        }
        if ( request.getDistrict() != null ) {
            vendor.setDistrict( request.getDistrict() );
        }
        if ( request.getEmail() != null ) {
            vendor.setEmail( request.getEmail() );
        }
        if ( request.getFax() != null ) {
            vendor.setFax( request.getFax() );
        }
        if ( request.getMobile() != null ) {
            vendor.setMobile( request.getMobile() );
        }
        if ( request.getName() != null ) {
            vendor.setName( request.getName() );
        }
        if ( request.getNotes() != null ) {
            vendor.setNotes( request.getNotes() );
        }
        if ( request.getPanNumber() != null ) {
            vendor.setPanNumber( request.getPanNumber() );
        }
        if ( request.getPaymentTermsDays() != null ) {
            vendor.setPaymentTermsDays( request.getPaymentTermsDays() );
        }
        if ( request.getPhone() != null ) {
            vendor.setPhone( request.getPhone() );
        }
        if ( request.getPhoto() != null ) {
            vendor.setPhoto( request.getPhoto() );
        }
        if ( request.getPostalCode() != null ) {
            vendor.setPostalCode( request.getPostalCode() );
        }
        if ( request.getState() != null ) {
            vendor.setState( request.getState() );
        }
        if ( request.getTaxNumber() != null ) {
            vendor.setTaxNumber( request.getTaxNumber() );
        }
    }
}

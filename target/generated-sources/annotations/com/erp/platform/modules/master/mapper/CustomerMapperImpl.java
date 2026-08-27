package com.erp.platform.modules.master.mapper;

import com.erp.platform.modules.master.dto.CreateCustomerRequest;
import com.erp.platform.modules.master.dto.CustomerDto;
import com.erp.platform.modules.master.dto.UpdateCustomerRequest;
import com.erp.platform.modules.master.entity.Customer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-27T10:05:34+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public Customer toEntity(CreateCustomerRequest request) {
        if ( request == null ) {
            return null;
        }

        Customer customer = new Customer();

        customer.setAadharNumber( request.getAadharNumber() );
        customer.setAccountDivision( request.getAccountDivision() );
        customer.setAccountHeads( request.getAccountHeads() );
        customer.setAddressLine1( request.getAddressLine1() );
        customer.setAddressLine2( request.getAddressLine2() );
        customer.setBankAccountNumber( request.getBankAccountNumber() );
        customer.setBankBranch( request.getBankBranch() );
        customer.setBankName( request.getBankName() );
        customer.setBillingAddress( request.getBillingAddress() );
        customer.setCategory( request.getCategory() );
        customer.setCity( request.getCity() );
        customer.setCode( request.getCode() );
        customer.setContactPerson( request.getContactPerson() );
        customer.setCountry( request.getCountry() );
        customer.setCreditLimit( request.getCreditLimit() );
        customer.setCurrencyCode( request.getCurrencyCode() );
        customer.setCustomerType( request.getCustomerType() );
        customer.setDepositsJson( request.getDepositsJson() );
        customer.setDistrict( request.getDistrict() );
        customer.setEmail( request.getEmail() );
        customer.setFax( request.getFax() );
        customer.setIfscCode( request.getIfscCode() );
        customer.setMobile( request.getMobile() );
        customer.setName( request.getName() );
        customer.setNotes( request.getNotes() );
        customer.setPanNumber( request.getPanNumber() );
        customer.setPaymentTermsDays( request.getPaymentTermsDays() );
        customer.setPhone( request.getPhone() );
        customer.setPhoto( request.getPhoto() );
        customer.setPostalCode( request.getPostalCode() );
        customer.setPreferredCourier( request.getPreferredCourier() );
        customer.setSalesArea( request.getSalesArea() );
        customer.setSalesPerson( request.getSalesPerson() );
        customer.setShippingAddress( request.getShippingAddress() );
        customer.setState( request.getState() );
        customer.setSubDealersJson( request.getSubDealersJson() );
        customer.setTaxNumber( request.getTaxNumber() );

        return customer;
    }

    @Override
    public CustomerDto toDto(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerDto customerDto = new CustomerDto();

        customerDto.setAadharNumber( customer.getAadharNumber() );
        customerDto.setAccountDivision( customer.getAccountDivision() );
        customerDto.setAccountHeads( customer.getAccountHeads() );
        customerDto.setActive( customer.isActive() );
        customerDto.setAddressLine1( customer.getAddressLine1() );
        customerDto.setAddressLine2( customer.getAddressLine2() );
        customerDto.setBankAccountNumber( customer.getBankAccountNumber() );
        customerDto.setBankBranch( customer.getBankBranch() );
        customerDto.setBankName( customer.getBankName() );
        customerDto.setBillingAddress( customer.getBillingAddress() );
        customerDto.setCategory( customer.getCategory() );
        customerDto.setCity( customer.getCity() );
        customerDto.setCode( customer.getCode() );
        customerDto.setContactPerson( customer.getContactPerson() );
        customerDto.setCountry( customer.getCountry() );
        customerDto.setCreatedAt( customer.getCreatedAt() );
        customerDto.setCreditLimit( customer.getCreditLimit() );
        customerDto.setCurrencyCode( customer.getCurrencyCode() );
        customerDto.setCustomerType( customer.getCustomerType() );
        customerDto.setDepositsJson( customer.getDepositsJson() );
        customerDto.setDistrict( customer.getDistrict() );
        customerDto.setEmail( customer.getEmail() );
        customerDto.setFax( customer.getFax() );
        customerDto.setId( customer.getId() );
        customerDto.setIfscCode( customer.getIfscCode() );
        customerDto.setMobile( customer.getMobile() );
        customerDto.setName( customer.getName() );
        customerDto.setNotes( customer.getNotes() );
        customerDto.setOutstandingBalance( customer.getOutstandingBalance() );
        customerDto.setPanNumber( customer.getPanNumber() );
        customerDto.setPaymentTermsDays( customer.getPaymentTermsDays() );
        customerDto.setPhone( customer.getPhone() );
        customerDto.setPhoto( customer.getPhoto() );
        customerDto.setPostalCode( customer.getPostalCode() );
        customerDto.setPreferredCourier( customer.getPreferredCourier() );
        customerDto.setSalesArea( customer.getSalesArea() );
        customerDto.setSalesPerson( customer.getSalesPerson() );
        customerDto.setShippingAddress( customer.getShippingAddress() );
        customerDto.setState( customer.getState() );
        customerDto.setSubDealersJson( customer.getSubDealersJson() );
        customerDto.setTaxNumber( customer.getTaxNumber() );
        customerDto.setTenantId( customer.getTenantId() );
        customerDto.setUpdatedAt( customer.getUpdatedAt() );

        return customerDto;
    }

    @Override
    public void updateEntity(UpdateCustomerRequest request, Customer customer) {
        if ( request == null ) {
            return;
        }

        if ( request.getAadharNumber() != null ) {
            customer.setAadharNumber( request.getAadharNumber() );
        }
        if ( request.getAccountDivision() != null ) {
            customer.setAccountDivision( request.getAccountDivision() );
        }
        if ( request.getAccountHeads() != null ) {
            customer.setAccountHeads( request.getAccountHeads() );
        }
        if ( request.getActive() != null ) {
            customer.setActive( request.getActive() );
        }
        if ( request.getAddressLine1() != null ) {
            customer.setAddressLine1( request.getAddressLine1() );
        }
        if ( request.getAddressLine2() != null ) {
            customer.setAddressLine2( request.getAddressLine2() );
        }
        if ( request.getBankAccountNumber() != null ) {
            customer.setBankAccountNumber( request.getBankAccountNumber() );
        }
        if ( request.getBankBranch() != null ) {
            customer.setBankBranch( request.getBankBranch() );
        }
        if ( request.getBankName() != null ) {
            customer.setBankName( request.getBankName() );
        }
        if ( request.getBillingAddress() != null ) {
            customer.setBillingAddress( request.getBillingAddress() );
        }
        if ( request.getCategory() != null ) {
            customer.setCategory( request.getCategory() );
        }
        if ( request.getCity() != null ) {
            customer.setCity( request.getCity() );
        }
        if ( request.getCode() != null ) {
            customer.setCode( request.getCode() );
        }
        if ( request.getContactPerson() != null ) {
            customer.setContactPerson( request.getContactPerson() );
        }
        if ( request.getCountry() != null ) {
            customer.setCountry( request.getCountry() );
        }
        if ( request.getCreditLimit() != null ) {
            customer.setCreditLimit( request.getCreditLimit() );
        }
        if ( request.getCurrencyCode() != null ) {
            customer.setCurrencyCode( request.getCurrencyCode() );
        }
        if ( request.getCustomerType() != null ) {
            customer.setCustomerType( request.getCustomerType() );
        }
        if ( request.getDepositsJson() != null ) {
            customer.setDepositsJson( request.getDepositsJson() );
        }
        if ( request.getDistrict() != null ) {
            customer.setDistrict( request.getDistrict() );
        }
        if ( request.getEmail() != null ) {
            customer.setEmail( request.getEmail() );
        }
        if ( request.getFax() != null ) {
            customer.setFax( request.getFax() );
        }
        if ( request.getIfscCode() != null ) {
            customer.setIfscCode( request.getIfscCode() );
        }
        if ( request.getMobile() != null ) {
            customer.setMobile( request.getMobile() );
        }
        if ( request.getName() != null ) {
            customer.setName( request.getName() );
        }
        if ( request.getNotes() != null ) {
            customer.setNotes( request.getNotes() );
        }
        if ( request.getPanNumber() != null ) {
            customer.setPanNumber( request.getPanNumber() );
        }
        if ( request.getPaymentTermsDays() != null ) {
            customer.setPaymentTermsDays( request.getPaymentTermsDays() );
        }
        if ( request.getPhone() != null ) {
            customer.setPhone( request.getPhone() );
        }
        if ( request.getPhoto() != null ) {
            customer.setPhoto( request.getPhoto() );
        }
        if ( request.getPostalCode() != null ) {
            customer.setPostalCode( request.getPostalCode() );
        }
        if ( request.getPreferredCourier() != null ) {
            customer.setPreferredCourier( request.getPreferredCourier() );
        }
        if ( request.getSalesArea() != null ) {
            customer.setSalesArea( request.getSalesArea() );
        }
        if ( request.getSalesPerson() != null ) {
            customer.setSalesPerson( request.getSalesPerson() );
        }
        if ( request.getShippingAddress() != null ) {
            customer.setShippingAddress( request.getShippingAddress() );
        }
        if ( request.getState() != null ) {
            customer.setState( request.getState() );
        }
        if ( request.getSubDealersJson() != null ) {
            customer.setSubDealersJson( request.getSubDealersJson() );
        }
        if ( request.getTaxNumber() != null ) {
            customer.setTaxNumber( request.getTaxNumber() );
        }
    }
}

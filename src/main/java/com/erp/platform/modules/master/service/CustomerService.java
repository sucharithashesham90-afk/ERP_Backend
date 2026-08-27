package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateCustomerRequest;
import com.erp.platform.modules.master.dto.CustomerDto;
import com.erp.platform.modules.master.dto.UpdateCustomerRequest;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.mapper.CustomerMapper;
import com.erp.platform.modules.master.repository.CustomerRepository;
import com.erp.platform.modules.accounting.service.PartyLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final TenantContext tenantContext;
    private final PartyLedgerService partyLedgerService;

    public PageResponse<CustomerDto> list(String search, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = StringUtils.hasText(search)
                ? customerRepository.searchByTenantId(tenantId, search, pageable)
                : customerRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(customerMapper::toDto));
    }

    public CustomerDto getById(UUID id) {
        return customerMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public CustomerDto create(CreateCustomerRequest request) {
        UUID tenantId = tenantContext.current();
        if (StringUtils.hasText(request.getEmail())
                && customerRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(tenantId, request.getEmail())) {
            throw AppException.conflict("Customer with email already exists: " + request.getEmail());
        }
        Customer customer = customerMapper.toEntity(request);
        customer.setTenantId(tenantId);
        if (!StringUtils.hasText(customer.getCode())) {
            customer.setCode("CUST-" + System.currentTimeMillis());
        }
        customer = customerRepository.save(customer);
        partyLedgerService.ensureLedger(PartyLedgerService.PartyType.CUSTOMER, customer.getName(), customer.getId(), customer.getCode());
        log.info("Customer created: id={}, name={}", customer.getId(), customer.getName());
        return customerMapper.toDto(customer);
    }

    @Transactional
    public CustomerDto update(UUID id, UpdateCustomerRequest request) {
        Customer customer = findOrThrow(id);
        customerMapper.updateEntity(request, customer);
        customer = customerRepository.save(customer);
        partyLedgerService.ensureLedger(PartyLedgerService.PartyType.CUSTOMER, customer.getName(), customer.getId(), customer.getCode());
        return customerMapper.toDto(customer);
    }

    @Transactional
    public void delete(UUID id) {
        Customer customer = findOrThrow(id);
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
        log.info("Customer soft-deleted: id={}", id);
    }

    private Customer findOrThrow(UUID id) {
        return customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Customer not found: " + id));
    }
}

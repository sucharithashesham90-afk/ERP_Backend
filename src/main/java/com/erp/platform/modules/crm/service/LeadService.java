package com.erp.platform.modules.crm.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.crm.entity.Lead;
import com.erp.platform.modules.crm.entity.Lead.LeadStatus;
import com.erp.platform.modules.crm.repository.LeadRepository;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LeadService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final TenantContext tenantContext;

    public PageResponse<Lead> list(LeadStatus status, String search, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (StringUtils.hasText(search)) {
            return PageResponse.of(leadRepository.searchByTenantId(tenantId, search, pageable));
        } else if (status != null) {
            return PageResponse.of(leadRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable));
        } else {
            return PageResponse.of(leadRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
        }
    }

    public Lead getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public Lead create(Lead lead) {
        UUID tenantId = tenantContext.current();
        lead.setTenantId(tenantId);
        if (lead.getStatus() == null) lead.setStatus(LeadStatus.NEW);
        if (lead.getPriority() == null) lead.setPriority(Lead.LeadPriority.MEDIUM);
        Lead saved = leadRepository.save(lead);
        log.info("Lead created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Lead update(UUID id, Lead body) {
        Lead existing = findOrThrow(id);
        existing.setName(body.getName());
        existing.setCompany(body.getCompany());
        existing.setEmail(body.getEmail());
        existing.setPhone(body.getPhone());
        existing.setSource(body.getSource());
        existing.setStatus(body.getStatus());
        existing.setPriority(body.getPriority());
        existing.setEstimatedValue(body.getEstimatedValue());
        existing.setIndustry(body.getIndustry());
        existing.setNotes(body.getNotes());
        existing.setExpectedCloseDate(body.getExpectedCloseDate());
        existing.setAssignedTo(body.getAssignedTo());
        return leadRepository.save(existing);
    }

    @Transactional
    public Lead updateStatus(UUID id, LeadStatus status) {
        Lead lead = findOrThrow(id);
        lead.setStatus(status);
        Lead saved = leadRepository.save(lead);
        if (status == LeadStatus.WON) {
            try {
                autoCreateCustomer(saved);
            } catch (Exception e) {
                log.warn("Auto-create customer skipped for lead {}: {}", saved.getId(), e.getMessage());
            }
        }
        return saved;
    }

    private void autoCreateCustomer(Lead lead) {
        UUID tenantId = tenantContext.current();
        // Skip if customer with same email already exists
        if (StringUtils.hasText(lead.getEmail())
                && customerRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(tenantId, lead.getEmail())) {
            log.info("Customer already exists for email {} — skipping auto-create from lead {}", lead.getEmail(), lead.getId());
            return;
        }
        String customerName = StringUtils.hasText(lead.getCompany()) ? lead.getCompany() : lead.getName();
        Customer customer = new Customer();
        customer.setTenantId(tenantId);
        customer.setName(customerName);
        customer.setEmail(lead.getEmail());
        customer.setPhone(lead.getPhone());
        customer.setContactPerson(lead.getName());
        customer.setCode("CUST-" + lead.getId().toString().substring(0, 8).toUpperCase());
        Customer saved = customerRepository.save(customer);
        log.info("Customer auto-created from WON lead {}: customerId={}, name={}", lead.getId(), saved.getId(), saved.getName());
    }

    @Transactional
    public void delete(UUID id) {
        Lead lead = findOrThrow(id);
        lead.setDeletedAt(LocalDateTime.now());
        leadRepository.save(lead);
        log.info("Lead soft-deleted: id={}", id);
    }

    public Map<String, Long> getStatusCounts() {
        UUID tenantId = tenantContext.current();
        return Map.of(
            "NEW",        leadRepository.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, LeadStatus.NEW),
            "CONTACTED",  leadRepository.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, LeadStatus.CONTACTED),
            "QUALIFIED",  leadRepository.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, LeadStatus.QUALIFIED),
            "WON",        leadRepository.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, LeadStatus.WON),
            "LOST",       leadRepository.countByTenantIdAndStatusAndDeletedAtIsNull(tenantId, LeadStatus.LOST)
        );
    }

    private Lead findOrThrow(UUID id) {
        return leadRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Lead not found: " + id));
    }
}

package com.erp.platform.modules.crm.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.crm.entity.Contact;
import com.erp.platform.modules.crm.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContactService {

    private final ContactRepository contactRepository;
    private final TenantContext tenantContext;

    public PageResponse<Contact> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(contactRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public Contact getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public Contact create(Contact contact) {
        UUID tenantId = tenantContext.current();
        contact.setTenantId(tenantId);
        Contact saved = contactRepository.save(contact);
        log.info("Contact created: id={}", saved.getId());
        return saved;
    }

    @Transactional
    public Contact update(UUID id, Contact body) {
        Contact existing = findOrThrow(id);
        existing.setFirstName(body.getFirstName());
        existing.setLastName(body.getLastName());
        existing.setEmail(body.getEmail());
        existing.setPhone(body.getPhone());
        existing.setMobile(body.getMobile());
        existing.setDesignation(body.getDesignation());
        existing.setDepartment(body.getDepartment());
        existing.setPrimary(body.isPrimary());
        existing.setNotes(body.getNotes());
        existing.setCustomerId(body.getCustomerId());
        return contactRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        Contact contact = findOrThrow(id);
        contact.setDeletedAt(LocalDateTime.now());
        contactRepository.save(contact);
        log.info("Contact soft-deleted: id={}", id);
    }

    private Contact findOrThrow(UUID id) {
        return contactRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Contact not found: " + id));
    }
}

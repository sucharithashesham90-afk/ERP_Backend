package com.erp.platform.modules.purchase.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.purchase.dto.CreatePaymentLiabilityRequest;
import com.erp.platform.modules.purchase.dto.PaymentLiabilityDto;
import com.erp.platform.modules.purchase.entity.PaymentLiability;
import com.erp.platform.modules.purchase.repository.PaymentLiabilityRepository;
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
public class PaymentLiabilityService {

    private final PaymentLiabilityRepository repository;
    private final com.erp.platform.modules.agri.repository.FarmerRepository farmerRepository;
    private final com.erp.platform.modules.agri.repository.OrganizerRepository organizerRepository;
    private final TenantContext tenantContext;

    public PageResponse<PaymentLiabilityDto> findAll(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable).map(this::toDto));
    }

    /** Unsettled liabilities matching the Liability Payment filters. */
    public java.util.List<PaymentLiabilityDto> searchForPosting(String partyType, String partyName,
            String intake, java.time.LocalDate from, java.time.LocalDate to) {
        UUID tenantId = tenantContext.current();
        return repository.searchForPosting(tenantId,
                        partyType == null ? "" : partyType.trim(),
                        partyName == null ? "" : partyName.trim(),
                        intake == null ? "" : intake.trim(),
                        from, to)
                .stream().map(this::toDto).toList();
    }

    /** Distinct grower/organizer names that hold liabilities, for the picker. */
    /**
     * Growers or organizers to choose between.
     *
     * <p>This used to return only the names already carrying a liability, which reads as sensible
     * and is unusable: with nothing owed yet the list is empty, so there is no way to pick a party
     * and no way to see that they owe nothing. The filter exists to narrow a search, and a search
     * that can only find what has already been found is not a filter.
     *
     * <p>Names come from the grower and organizer masters, with anyone already holding a liability
     * merged in — a party can be renamed or archived after the liability was raised, and that row
     * still has to be findable.
     */
    public java.util.List<String> partyNames(String partyType) {
        UUID tenantId = tenantContext.current();
        String type = partyType == null ? "" : partyType.trim().toUpperCase();

        java.util.SortedSet<String> names = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        var all = org.springframework.data.domain.PageRequest.of(0, 5000);
        if (type.isEmpty() || type.contains("ORGANIZER")) {
            organizerRepository.findByTenantIdAndDeletedAtIsNull(tenantId, all)
                    .forEach(o -> { if (o.getName() != null && !o.getName().isBlank()) names.add(o.getName()); });
        }
        if (type.isEmpty() || type.contains("GROWER") || type.contains("FARMER")) {
            farmerRepository.findByTenantIdAndDeletedAtIsNull(tenantId, all)
                    .forEach(f -> { if (f.getName() != null && !f.getName().isBlank()) names.add(f.getName()); });
        }

        // Anyone with a liability on file stays selectable even if their master record has gone.
        // Filtered rather than added blind: this set is ordered by a comparator, and a comparator
        // dereferences what it is given — so a single null name from an old row takes the whole
        // endpoint down with a NullPointerException. It did.
        for (String n : repository.findPartyNames(tenantId, type)) {
            if (n != null && !n.isBlank()) names.add(n);
        }
        return new java.util.ArrayList<>(names);
    }

    public PaymentLiabilityDto findById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PaymentLiabilityDto create(CreatePaymentLiabilityRequest request) {
        UUID tenantId = tenantContext.current();
        PaymentLiability entity = new PaymentLiability();
        entity.setTenantId(tenantId);
        entity.setLiabilityNumber(request.liabilityNumber());
        entity.setPartyType(request.partyType());
        entity.setPartyName(request.partyName());
        entity.setVendorCode(request.vendorCode());
        entity.setLotNumber(request.lotNumber());
        entity.setLiabilityFromDate(request.liabilityFromDate());
        entity.setLiabilityToDate(request.liabilityToDate());
        entity.setTotalLiability(request.totalLiability());
        entity.setPaidAmount(request.paidAmount());
        entity.setBalance(request.balance());
        entity.setStatus(request.status() != null ? request.status() : "PENDING");
        entity = repository.save(entity);
        log.info("PaymentLiability created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public PaymentLiabilityDto update(UUID id, CreatePaymentLiabilityRequest request) {
        PaymentLiability entity = findOrThrow(id);
        entity.setLiabilityNumber(request.liabilityNumber());
        entity.setPartyType(request.partyType());
        entity.setPartyName(request.partyName());
        entity.setVendorCode(request.vendorCode());
        entity.setLotNumber(request.lotNumber());
        entity.setLiabilityFromDate(request.liabilityFromDate());
        entity.setLiabilityToDate(request.liabilityToDate());
        entity.setTotalLiability(request.totalLiability());
        entity.setPaidAmount(request.paidAmount());
        entity.setBalance(request.balance());
        entity.setStatus(request.status());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PaymentLiability entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private PaymentLiability findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("PaymentLiability not found: " + id));
    }

    private PaymentLiabilityDto toDto(PaymentLiability e) {
        return new PaymentLiabilityDto(
                e.getId(),
                e.getLiabilityNumber(),
                e.getPartyType(),
                e.getPartyName(),
                e.getVendorCode(),
                e.getLotNumber(),
                e.getLiabilityFromDate(),
                e.getLiabilityToDate(),
                e.getTotalLiability(),
                e.getPaidAmount(),
                e.getBalance(),
                e.getStatus()
        );
    }
}

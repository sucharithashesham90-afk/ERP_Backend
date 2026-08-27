package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.JournalEntryService;
import com.erp.platform.modules.inventory.dto.CreateScrapDisposalRequest;
import com.erp.platform.modules.inventory.dto.CreateScrapEntryRequest;
import com.erp.platform.modules.inventory.dto.ScrapDisposalDto;
import com.erp.platform.modules.inventory.dto.ScrapEntryDto;
import com.erp.platform.modules.inventory.entity.ScrapDisposal;
import com.erp.platform.modules.inventory.entity.ScrapEntry;
import com.erp.platform.modules.inventory.entity.ScrapEntry.ScrapStatus;
import com.erp.platform.modules.inventory.repository.ScrapDisposalRepository;
import com.erp.platform.modules.inventory.repository.ScrapEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScrapEntryService {

    private final ScrapEntryRepository scrapEntryRepository;
    private final ScrapDisposalRepository scrapDisposalRepository;
    private final StockService stockService;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;

    public PageResponse<ScrapEntryDto> list(ScrapStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<ScrapEntry> page = status != null
                ? scrapEntryRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable)
                : scrapEntryRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public ScrapEntryDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ScrapEntryDto create(CreateScrapEntryRequest request) {
        UUID tenantId = tenantContext.current();
        ScrapEntry entry = new ScrapEntry();
        entry.setTenantId(tenantId);
        entry.setScrapNumber(generateScrapNumber(tenantId));
        entry.setScrapDate(request.getScrapDate());
        entry.setScrapType(request.getScrapType());
        entry.setSourceType(request.getSourceType());
        entry.setSourceId(request.getSourceId());
        entry.setWarehouseId(request.getWarehouseId());
        entry.setWarehouseName(request.getWarehouseName());
        entry.setProductId(request.getProductId());
        entry.setProductName(request.getProductName());
        entry.setLotNumber(request.getLotNumber());
        entry.setQuantity(request.getQuantity());
        entry.setUnit(request.getUnit());
        entry.setScrapValue(request.getScrapValue());
        entry.setRecoveryValue(request.getRecoveryValue() != null ? request.getRecoveryValue() : BigDecimal.ZERO);
        entry.setReason(request.getReason());
        entry.setNotes(request.getNotes());
        entry.setStatus(ScrapStatus.DRAFT);
        ScrapEntry saved = scrapEntryRepository.save(entry);
        log.info("ScrapEntry created: id={}, number={}", saved.getId(), saved.getScrapNumber());
        return toDto(saved);
    }

    @Transactional
    public ScrapEntryDto update(UUID id, CreateScrapEntryRequest request) {
        ScrapEntry entry = findOrThrow(id);
        if (entry.getStatus() != ScrapStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT scrap entries can be edited");
        }
        entry.setScrapDate(request.getScrapDate());
        entry.setScrapType(request.getScrapType());
        entry.setSourceType(request.getSourceType());
        entry.setSourceId(request.getSourceId());
        entry.setWarehouseId(request.getWarehouseId());
        entry.setWarehouseName(request.getWarehouseName());
        entry.setProductId(request.getProductId());
        entry.setProductName(request.getProductName());
        entry.setLotNumber(request.getLotNumber());
        entry.setQuantity(request.getQuantity());
        entry.setUnit(request.getUnit());
        entry.setScrapValue(request.getScrapValue());
        entry.setRecoveryValue(request.getRecoveryValue() != null ? request.getRecoveryValue() : BigDecimal.ZERO);
        entry.setReason(request.getReason());
        entry.setNotes(request.getNotes());
        return toDto(scrapEntryRepository.save(entry));
    }

    @Transactional
    public ScrapEntryDto approve(UUID id, String approvedBy) {
        ScrapEntry entry = findOrThrow(id);
        if (entry.getStatus() != ScrapStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT scrap entries can be approved");
        }
        // Deduct scrapped material from inventory
        if (entry.getProductId() != null && entry.getQuantity() != null
                && entry.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            try {
                stockService.deductStock(entry.getProductId(), entry.getWarehouseId(), entry.getQuantity(),
                        "SCRAP", entry.getId(), entry.getScrapNumber(),
                        "RAW", "DAMAGED", entry.getLotNumber(), "SCRAP");
                log.info("Stock deducted for scrap {}: product={}, qty={}",
                        entry.getScrapNumber(), entry.getProductId(), entry.getQuantity());
            } catch (Exception e) {
                log.warn("Stock deduction skipped for scrap {}: {}", entry.getScrapNumber(), e.getMessage());
            }
        }
        entry.setStatus(ScrapStatus.APPROVED);
        entry.setApprovedBy(approvedBy);
        return toDto(scrapEntryRepository.save(entry));
    }

    @Transactional
    public ScrapEntryDto recordDisposal(UUID scrapEntryId, CreateScrapDisposalRequest request) {
        UUID tenantId = tenantContext.current();
        ScrapEntry entry = findOrThrow(scrapEntryId);
        if (entry.getStatus() != ScrapStatus.APPROVED) {
            throw AppException.badRequest("Only APPROVED scrap entries can have a disposal recorded");
        }
        BigDecimal disposalAmount = request.getDisposalAmount() != null ? request.getDisposalAmount() : BigDecimal.ZERO;
        BigDecimal transportCost = request.getTransportCost() != null ? request.getTransportCost() : BigDecimal.ZERO;
        BigDecimal netRecovery = disposalAmount.subtract(transportCost);

        ScrapDisposal disposal = new ScrapDisposal();
        disposal.setTenantId(tenantId);
        disposal.setScrapEntry(entry);
        disposal.setDisposalDate(request.getDisposalDate());
        disposal.setDisposalMethod(request.getDisposalMethod());
        disposal.setDisposedTo(request.getDisposedTo());
        disposal.setDisposalAmount(disposalAmount);
        disposal.setTransportCost(transportCost);
        disposal.setNetRecovery(netRecovery);
        disposal.setReferenceNumber(request.getReferenceNumber());
        disposal.setNotes(request.getNotes());
        scrapDisposalRepository.save(disposal);

        entry.setStatus(ScrapStatus.DISPOSED);
        entry.setRecoveryValue(netRecovery);
        entry.setDisposedAt(LocalDateTime.now());
        scrapEntryRepository.save(entry);

        // Write-off journal entry: DR Scrap Expense / CR Inventory Asset
        try {
            createScrapWriteOffJournalEntry(tenantId, entry);
        } catch (Exception ex) {
            log.warn("Scrap write-off JE skipped for {}: {}", entry.getScrapNumber(), ex.getMessage());
        }

        log.info("ScrapDisposal recorded for entry id={}", scrapEntryId);
        return toDto(entry);
    }

    private void createScrapWriteOffJournalEntry(UUID tenantId, ScrapEntry entry) {
        List<Account> expAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "SCRAP_EXPENSE");
        List<Account> invAccounts = accountRepository.findByTenantIdAndSubTypeAndDeletedAtIsNull(tenantId, "INVENTORY_ASSET");
        if (expAccounts.isEmpty() || invAccounts.isEmpty()) {
            log.debug("Scrap write-off JE skipped for {}: accounts not configured", entry.getScrapNumber());
            return;
        }
        BigDecimal amount = entry.getScrapValue() != null ? entry.getScrapValue() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) == 0) return;
        Account expAccount = expAccounts.get(0);
        Account invAccount = invAccounts.get(0);

        JournalEntry je = new JournalEntry();
        je.setTenantId(tenantId);
        je.setReferenceType("SCRAP");
        je.setReferenceId(entry.getId());
        je.setReferenceNumber(entry.getScrapNumber());
        je.setDescription("Scrap write-off: " + entry.getScrapNumber() + " — " + entry.getProductName());
        je.setEntryDate(entry.getScrapDate() != null ? entry.getScrapDate() : LocalDate.now());

        JournalEntryLine drLine = new JournalEntryLine();
        drLine.setAccountId(expAccount.getId());
        drLine.setAccountCode(expAccount.getCode());
        drLine.setAccountName(expAccount.getName());
        drLine.setDebitAmount(amount);
        drLine.setCreditAmount(BigDecimal.ZERO);
        drLine.setDescription("Scrap expense — " + entry.getScrapNumber());

        JournalEntryLine crLine = new JournalEntryLine();
        crLine.setAccountId(invAccount.getId());
        crLine.setAccountCode(invAccount.getCode());
        crLine.setAccountName(invAccount.getName());
        crLine.setDebitAmount(BigDecimal.ZERO);
        crLine.setCreditAmount(amount);
        crLine.setDescription("Inventory write-off — " + entry.getScrapNumber());

        je.getLines().add(drLine);
        je.getLines().add(crLine);
        journalEntryService.create(je);
        log.info("Scrap write-off JE (DRAFT) created for {}", entry.getScrapNumber());
    }

    @Transactional
    public void delete(UUID id) {
        ScrapEntry entry = findOrThrow(id);
        if (entry.getStatus() != ScrapStatus.DRAFT) {
            throw AppException.badRequest("Only DRAFT scrap entries can be deleted");
        }
        entry.setDeletedAt(LocalDateTime.now());
        scrapEntryRepository.save(entry);
        log.info("ScrapEntry soft-deleted: id={}", id);
    }

    private ScrapEntry findOrThrow(UUID id) {
        return scrapEntryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Scrap entry not found: " + id));
    }

    private String generateScrapNumber(UUID tenantId) {
        long count = scrapEntryRepository.countByTenantId(tenantId);
        return String.format("SCR-%d-%03d", Year.now().getValue(), count + 1);
    }

    private ScrapEntryDto toDto(ScrapEntry e) {
        ScrapEntryDto dto = new ScrapEntryDto();
        dto.setId(e.getId());
        dto.setScrapNumber(e.getScrapNumber());
        dto.setScrapDate(e.getScrapDate());
        dto.setScrapType(e.getScrapType());
        dto.setSourceType(e.getSourceType());
        dto.setSourceId(e.getSourceId());
        dto.setWarehouseId(e.getWarehouseId());
        dto.setWarehouseName(e.getWarehouseName());
        dto.setProductId(e.getProductId());
        dto.setProductName(e.getProductName());
        dto.setLotNumber(e.getLotNumber());
        dto.setQuantity(e.getQuantity());
        dto.setUnit(e.getUnit());
        dto.setScrapValue(e.getScrapValue());
        dto.setRecoveryValue(e.getRecoveryValue());
        dto.setReason(e.getReason());
        dto.setStatus(e.getStatus());
        dto.setApprovedBy(e.getApprovedBy());
        dto.setDisposedAt(e.getDisposedAt());
        dto.setNotes(e.getNotes());
        dto.setCreatedAt(e.getCreatedAt());

        scrapDisposalRepository.findByTenantIdAndScrapEntry_IdAndDeletedAtIsNull(
                e.getTenantId(), e.getId()).ifPresent(d -> {
            ScrapDisposalDto dd = new ScrapDisposalDto();
            dd.setId(d.getId());
            dd.setDisposalDate(d.getDisposalDate());
            dd.setDisposalMethod(d.getDisposalMethod());
            dd.setDisposedTo(d.getDisposedTo());
            dd.setDisposalAmount(d.getDisposalAmount());
            dd.setTransportCost(d.getTransportCost());
            dd.setNetRecovery(d.getNetRecovery());
            dd.setReferenceNumber(d.getReferenceNumber());
            dd.setNotes(d.getNotes());
            dto.setDisposal(dd);
        });
        return dto;
    }
}

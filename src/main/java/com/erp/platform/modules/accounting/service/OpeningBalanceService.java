package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.dto.CreateOpeningBalanceRequest;
import com.erp.platform.modules.accounting.dto.MigrationBatchDto;
import com.erp.platform.modules.accounting.dto.OpeningBalanceDto;
import com.erp.platform.modules.accounting.entity.MigrationBatch;
import com.erp.platform.modules.accounting.entity.MigrationBatch.BatchStatus;
import com.erp.platform.modules.accounting.entity.OpeningBalance;
import com.erp.platform.modules.accounting.entity.OpeningBalance.BalanceType;
import com.erp.platform.modules.accounting.entity.OpeningBalance.MigrationStatus;
import com.erp.platform.modules.accounting.repository.MigrationBatchRepository;
import com.erp.platform.modules.accounting.repository.OpeningBalanceRepository;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OpeningBalanceService {

    private final OpeningBalanceRepository openingBalanceRepository;
    private final MigrationBatchRepository migrationBatchRepository;
    private final AccountRepository accountRepository;
    private final TenantContext tenantContext;

    public PageResponse<OpeningBalanceDto> list(BalanceType balanceType, MigrationStatus status, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<OpeningBalance> page;
        if (balanceType != null) {
            page = openingBalanceRepository.findByTenantIdAndBalanceTypeAndDeletedAtIsNull(tenantId, balanceType, pageable);
        } else if (status != null) {
            page = openingBalanceRepository.findByTenantIdAndStatusAndDeletedAtIsNull(tenantId, status, pageable);
        } else {
            page = openingBalanceRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }
        return PageResponse.of(page.map(this::toDto));
    }

    public OpeningBalanceDto getById(UUID id) {
        return toDto(findBalanceOrThrow(id));
    }

    @Transactional
    public MigrationBatchDto createBatch(String description, LocalDate asOfDate, List<CreateOpeningBalanceRequest> lines) {
        UUID tenantId = tenantContext.current();
        long count = migrationBatchRepository.countByTenantId(tenantId);
        String batchNumber = String.format("MB-%d-%03d", Year.now().getValue(), count + 1);

        MigrationBatch batch = new MigrationBatch();
        batch.setTenantId(tenantId);
        batch.setBatchNumber(batchNumber);
        batch.setDescription(description);
        batch.setAsOfDate(asOfDate);
        batch.setStatus(BatchStatus.IN_PROGRESS);
        batch.setTotalRecords(lines.size());
        batch.setProcessedRecords(0);
        batch.setErrorRecords(0);
        batch = migrationBatchRepository.save(batch);

        int processed = 0;
        int errors = 0;
        for (CreateOpeningBalanceRequest line : lines) {
            try {
                OpeningBalance ob = buildBalance(tenantId, batchNumber, asOfDate, line);
                openingBalanceRepository.save(ob);
                processed++;
            } catch (Exception e) {
                log.error("Error processing balance line: {}", e.getMessage());
                errors++;
            }
        }

        batch.setProcessedRecords(processed);
        batch.setErrorRecords(errors);
        batch.setStatus(errors == 0 ? BatchStatus.COMPLETED : BatchStatus.FAILED);
        batch.setCompletedAt(LocalDateTime.now());
        batch = migrationBatchRepository.save(batch);

        log.info("Migration batch created: batchNumber={}, processed={}, errors={}", batchNumber, processed, errors);
        return toBatchDto(batch);
    }

    @Transactional
    public MigrationBatchDto validateBatch(String batchNumber) {
        UUID tenantId = tenantContext.current();
        MigrationBatch batch = findBatchOrThrow(tenantId, batchNumber);

        List<OpeningBalance> balances = openingBalanceRepository
                .findByTenantIdAndMigrationRefAndDeletedAtIsNull(tenantId, batchNumber);

        balances.stream()
                .filter(b -> b.getStatus() == MigrationStatus.DRAFT)
                .forEach(b -> {
                    b.setStatus(MigrationStatus.VALIDATED);
                    openingBalanceRepository.save(b);
                });

        log.info("Batch validated: batchNumber={}", batchNumber);
        return toBatchDto(batch);
    }

    @Transactional
    public MigrationBatchDto postBatch(String batchNumber) {
        UUID tenantId = tenantContext.current();
        MigrationBatch batch = findBatchOrThrow(tenantId, batchNumber);

        List<OpeningBalance> balances = openingBalanceRepository
                .findByTenantIdAndMigrationRefAndDeletedAtIsNull(tenantId, batchNumber);

        long draftCount = balances.stream().filter(b -> b.getStatus() == MigrationStatus.DRAFT).count();
        if (draftCount > 0) {
            throw AppException.businessRule("Batch contains " + draftCount + " unvalidated records. Please validate first.");
        }

        LocalDateTime now = LocalDateTime.now();
        balances.stream()
                .filter(b -> b.getStatus() == MigrationStatus.VALIDATED)
                .forEach(b -> {
                    b.setStatus(MigrationStatus.POSTED);
                    b.setPostedAt(now);
                    openingBalanceRepository.save(b);
                    if (b.getAccountId() != null) {
                        accountRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, b.getAccountId()).ifPresent(acct -> {
                            java.math.BigDecimal dr = b.getDebitAmount() != null ? b.getDebitAmount() : java.math.BigDecimal.ZERO;
                            java.math.BigDecimal cr = b.getCreditAmount() != null ? b.getCreditAmount() : java.math.BigDecimal.ZERO;
                            acct.setBalance(acct.getBalance() != null
                                    ? acct.getBalance().add(dr).subtract(cr)
                                    : dr.subtract(cr));
                            accountRepository.save(acct);
                        });
                    }
                });

        log.info("Batch posted: batchNumber={}", batchNumber);
        return toBatchDto(batch);
    }

    @Transactional
    public MigrationBatchDto reverseBatch(String batchNumber) {
        UUID tenantId = tenantContext.current();
        MigrationBatch batch = findBatchOrThrow(tenantId, batchNumber);

        List<OpeningBalance> balances = openingBalanceRepository
                .findByTenantIdAndMigrationRefAndDeletedAtIsNull(tenantId, batchNumber);

        balances.stream()
                .filter(b -> b.getStatus() == MigrationStatus.POSTED)
                .forEach(b -> {
                    b.setStatus(MigrationStatus.REVERSED);
                    openingBalanceRepository.save(b);
                });

        log.info("Batch reversed: batchNumber={}", batchNumber);
        return toBatchDto(batch);
    }

    @Transactional
    public void delete(UUID id) {
        OpeningBalance balance = findBalanceOrThrow(id);
        if (balance.getStatus() != MigrationStatus.DRAFT) {
            throw AppException.businessRule("Only DRAFT balances can be deleted. Current status: " + balance.getStatus());
        }
        balance.setDeletedAt(LocalDateTime.now());
        openingBalanceRepository.save(balance);
        log.info("Opening balance soft-deleted: id={}", id);
    }

    private OpeningBalance buildBalance(UUID tenantId, String batchNumber, LocalDate asOfDate, CreateOpeningBalanceRequest req) {
        OpeningBalance ob = new OpeningBalance();
        ob.setTenantId(tenantId);
        ob.setMigrationRef(batchNumber);
        ob.setBalanceType(req.getBalanceType());
        ob.setAsOfDate(asOfDate);
        ob.setAccountId(req.getAccountId());
        ob.setAccountCode(req.getAccountCode());
        ob.setAccountName(req.getAccountName());
        ob.setPartyId(req.getPartyId());
        ob.setPartyName(req.getPartyName());
        ob.setPartyType(req.getPartyType());
        ob.setProductId(req.getProductId());
        ob.setProductName(req.getProductName());
        ob.setWarehouseId(req.getWarehouseId());
        ob.setWarehouseName(req.getWarehouseName());
        ob.setQuantity(req.getQuantity());
        ob.setUnitCost(req.getUnitCost());
        ob.setDebitAmount(req.getDebitAmount() != null ? req.getDebitAmount() : java.math.BigDecimal.ZERO);
        ob.setCreditAmount(req.getCreditAmount() != null ? req.getCreditAmount() : java.math.BigDecimal.ZERO);
        ob.setCurrency(req.getCurrency() != null ? req.getCurrency() : "INR");
        ob.setReference(req.getReference());
        ob.setStatus(MigrationStatus.DRAFT);
        ob.setNotes(req.getNotes());
        return ob;
    }

    private MigrationBatch findBatchOrThrow(UUID tenantId, String batchNumber) {
        return migrationBatchRepository.findByTenantIdAndBatchNumberAndDeletedAtIsNull(tenantId, batchNumber)
                .orElseThrow(() -> AppException.notFound("Migration batch not found: " + batchNumber));
    }

    private OpeningBalance findBalanceOrThrow(UUID id) {
        return openingBalanceRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Opening balance not found: " + id));
    }

    private OpeningBalanceDto toDto(OpeningBalance ob) {
        OpeningBalanceDto dto = new OpeningBalanceDto();
        dto.setId(ob.getId());
        dto.setTenantId(ob.getTenantId());
        dto.setMigrationRef(ob.getMigrationRef());
        dto.setBalanceType(ob.getBalanceType());
        dto.setAsOfDate(ob.getAsOfDate());
        dto.setAccountId(ob.getAccountId());
        dto.setAccountCode(ob.getAccountCode());
        dto.setAccountName(ob.getAccountName());
        dto.setPartyId(ob.getPartyId());
        dto.setPartyName(ob.getPartyName());
        dto.setPartyType(ob.getPartyType());
        dto.setProductId(ob.getProductId());
        dto.setProductName(ob.getProductName());
        dto.setWarehouseId(ob.getWarehouseId());
        dto.setWarehouseName(ob.getWarehouseName());
        dto.setQuantity(ob.getQuantity());
        dto.setUnitCost(ob.getUnitCost());
        dto.setDebitAmount(ob.getDebitAmount());
        dto.setCreditAmount(ob.getCreditAmount());
        dto.setCurrency(ob.getCurrency());
        dto.setReference(ob.getReference());
        dto.setStatus(ob.getStatus());
        dto.setPostedAt(ob.getPostedAt());
        dto.setNotes(ob.getNotes());
        dto.setCreatedAt(ob.getCreatedAt());
        return dto;
    }

    private MigrationBatchDto toBatchDto(MigrationBatch b) {
        MigrationBatchDto dto = new MigrationBatchDto();
        dto.setId(b.getId());
        dto.setTenantId(b.getTenantId());
        dto.setBatchNumber(b.getBatchNumber());
        dto.setDescription(b.getDescription());
        dto.setAsOfDate(b.getAsOfDate());
        dto.setStatus(b.getStatus());
        dto.setTotalRecords(b.getTotalRecords());
        dto.setProcessedRecords(b.getProcessedRecords());
        dto.setErrorRecords(b.getErrorRecords());
        dto.setCompletedAt(b.getCompletedAt());
        dto.setNotes(b.getNotes());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }
}

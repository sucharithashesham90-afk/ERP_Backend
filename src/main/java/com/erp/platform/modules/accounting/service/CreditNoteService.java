package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.CreditNote;
import com.erp.platform.modules.accounting.entity.CreditNoteLine;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.CreditNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CreditNoteService {

    private final CreditNoteRepository creditNoteRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryService journalEntryService;
    private final TenantContext tenantContext;

    public PageResponse<Map<String, Object>> list(String type, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = (type != null && !type.isBlank())
                ? creditNoteRepository.findByTenantIdAndNoteTypeAndDeletedAtIsNull(tenantId, type, pageable)
                : creditNoteRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toMap));
    }

    public Map<String, Object> getById(UUID id) {
        UUID tenantId = tenantContext.current();
        return toMap(creditNoteRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Credit note not found: " + id)));
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> create(Map<String, Object> request) {
        UUID tenantId = tenantContext.current();

        CreditNote cn = new CreditNote();
        cn.setTenantId(tenantId);
        cn.setCreditNoteNumber(generateNumber());
        cn.setNoteType((String) request.getOrDefault("noteType", "DIRECT"));
        String dateStr = (String) request.get("noteDate");
        cn.setNoteDate(dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now());
        cn.setBillNo((String) request.get("billNo"));
        cn.setPartyName((String) request.get("partyName"));
        cn.setNarration((String) request.get("narration"));
        cn.setStatus("DRAFT");

        List<Map<String, Object>> rawLines = (List<Map<String, Object>>) request.get("lines");
        BigDecimal total = BigDecimal.ZERO;
        if (rawLines != null) {
            List<CreditNoteLine> lines = new ArrayList<>();
            for (Map<String, Object> rl : rawLines) {
                CreditNoteLine line = new CreditNoteLine();
                line.setCreditNote(cn);
                line.setSide((String) rl.get("side"));
                line.setAccountCode((String) rl.get("accountCode"));
                line.setAccountName((String) rl.get("accountName"));
                BigDecimal amt = new BigDecimal(rl.getOrDefault("amount", "0").toString());
                line.setAmount(amt);
                line.setNarration((String) rl.get("narration"));
                line.setBillNo((String) rl.get("billNo"));
                if ("CREDIT".equals(line.getSide())) total = total.add(amt);
                lines.add(line);
            }
            cn.setLines(lines);
            cn.setTotalAmount(total);
        }

        cn = creditNoteRepository.save(cn);
        log.info("Credit note created: {} ({})", cn.getCreditNoteNumber(), cn.getId());
        return toMap(cn);
    }

    @Transactional
    public Map<String, Object> post(UUID id) {
        UUID tenantId = tenantContext.current();
        CreditNote cn = creditNoteRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Credit note not found: " + id));
        if (!"DRAFT".equals(cn.getStatus())) throw AppException.badRequest("Only DRAFT credit notes can be posted");

        try {
            JournalEntry je = new JournalEntry();
            je.setTenantId(tenantId);
            je.setReferenceType("CREDIT_NOTE");
            je.setReferenceId(cn.getId());
            je.setReferenceNumber(cn.getCreditNoteNumber());
            je.setDescription("Credit Note: " + cn.getCreditNoteNumber()
                    + (cn.getPartyName() != null ? " — " + cn.getPartyName() : ""));
            je.setEntryDate(cn.getNoteDate());
            for (CreditNoteLine l : cn.getLines()) {
                JournalEntryLine jel = new JournalEntryLine();
                jel.setAccountId(l.getAccountId());
                jel.setAccountCode(l.getAccountCode());
                jel.setAccountName(l.getAccountName());
                if ("DEBIT".equals(l.getSide())) {
                    jel.setDebitAmount(l.getAmount());
                    jel.setCreditAmount(BigDecimal.ZERO);
                } else {
                    jel.setDebitAmount(BigDecimal.ZERO);
                    jel.setCreditAmount(l.getAmount());
                }
                jel.setDescription(l.getNarration());
                je.getLines().add(jel);
            }
            JournalEntry saved = journalEntryService.create(je);
            cn.setJournalEntryId(saved.getId());
        } catch (Exception e) {
            log.warn("Credit note JE skipped for {}: {}", cn.getCreditNoteNumber(), e.getMessage());
        }

        cn.setStatus("POSTED");
        cn = creditNoteRepository.save(cn);
        log.info("Credit note posted: {}", cn.getCreditNoteNumber());
        return toMap(cn);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        CreditNote cn = creditNoteRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Credit note not found: " + id));
        if ("POSTED".equals(cn.getStatus())) throw AppException.badRequest("Cannot delete a posted credit note");
        cn.setDeletedAt(LocalDateTime.now());
        creditNoteRepository.save(cn);
    }

    private Map<String, Object> toMap(CreditNote cn) {
        List<Map<String, Object>> lines = cn.getLines() == null ? List.of()
                : cn.getLines().stream().map(l -> Map.<String, Object>of(
                        "id", l.getId(),
                        "side", l.getSide() == null ? "" : l.getSide(),
                        "accountCode", l.getAccountCode() == null ? "" : l.getAccountCode(),
                        "accountName", l.getAccountName() == null ? "" : l.getAccountName(),
                        "amount", l.getAmount(),
                        "narration", l.getNarration() == null ? "" : l.getNarration(),
                        "billNo", l.getBillNo() == null ? "" : l.getBillNo()
                )).collect(Collectors.toList());

        Map<String, Object> dto = new java.util.LinkedHashMap<>();
        dto.put("id",               cn.getId());
        dto.put("tenantId",         cn.getTenantId());
        dto.put("creditNoteNumber", cn.getCreditNoteNumber());
        dto.put("noteType",         cn.getNoteType() == null ? "DIRECT" : cn.getNoteType());
        dto.put("noteDate",         cn.getNoteDate() == null ? "" : cn.getNoteDate().toString());
        dto.put("billNo",           cn.getBillNo() == null ? "" : cn.getBillNo());
        dto.put("partyName",        cn.getPartyName() == null ? "" : cn.getPartyName());
        dto.put("narration",        cn.getNarration() == null ? "" : cn.getNarration());
        dto.put("totalAmount",      cn.getTotalAmount());
        dto.put("status",           cn.getStatus());
        dto.put("lines",            lines);
        dto.put("createdAt",        cn.getCreatedAt() == null ? "" : cn.getCreatedAt().toString());
        return dto;
    }

    private String generateNumber() {
        return "CN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

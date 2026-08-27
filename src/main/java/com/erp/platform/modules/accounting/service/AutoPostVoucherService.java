package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.accounting.entity.AutoPostVoucher;
import com.erp.platform.modules.accounting.repository.AutoPostVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutoPostVoucherService {

    private final AutoPostVoucherRepository autoPostVoucherRepository;
    private final TenantContext tenantContext;

    public PageResponse<Map<String, Object>> list(Pageable pageable) {
        return PageResponse.of(autoPostVoucherRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toMap));
    }

    public Map<String, Object> getById(UUID id) {
        return toMap(autoPostVoucherRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("AutoPostVoucher not found: " + id)));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> req) {
        AutoPostVoucher e = new AutoPostVoucher();
        e.setTenantId(tenantContext.current());
        e.setName((String) req.get("name"));
        e.setTriggerType((String) req.get("triggerType"));
        if (req.get("debitLedgerId") != null) e.setDebitLedgerId(UUID.fromString((String) req.get("debitLedgerId")));
        if (req.get("creditLedgerId") != null) e.setCreditLedgerId(UUID.fromString((String) req.get("creditLedgerId")));
        if (req.get("amount") != null) e.setAmount(new BigDecimal(req.get("amount").toString()));
        e.setNarration((String) req.get("narration"));
        if (req.get("dayOfMonth") != null) e.setDayOfMonth(Integer.parseInt(req.get("dayOfMonth").toString()));
        if (req.get("nextRunDate") != null) e.setNextRunDate(LocalDate.parse((String) req.get("nextRunDate")));
        e.setActive(req.get("active") == null || Boolean.TRUE.equals(req.get("active")));
        return toMap(autoPostVoucherRepository.save(e));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> req) {
        AutoPostVoucher e = autoPostVoucherRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("AutoPostVoucher not found: " + id));
        e.setName((String) req.get("name"));
        e.setTriggerType((String) req.get("triggerType"));
        if (req.get("debitLedgerId") != null) e.setDebitLedgerId(UUID.fromString((String) req.get("debitLedgerId")));
        if (req.get("creditLedgerId") != null) e.setCreditLedgerId(UUID.fromString((String) req.get("creditLedgerId")));
        if (req.get("amount") != null) e.setAmount(new BigDecimal(req.get("amount").toString()));
        e.setNarration((String) req.get("narration"));
        if (req.get("dayOfMonth") != null) e.setDayOfMonth(Integer.parseInt(req.get("dayOfMonth").toString()));
        if (req.get("nextRunDate") != null) e.setNextRunDate(LocalDate.parse((String) req.get("nextRunDate")));
        if (req.get("active") != null) e.setActive(Boolean.TRUE.equals(req.get("active")));
        return toMap(autoPostVoucherRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        AutoPostVoucher e = autoPostVoucherRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("AutoPostVoucher not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        autoPostVoucherRepository.save(e);
    }

    private Map<String, Object> toMap(AutoPostVoucher e) {
        return Map.of(
                "id", e.getId(), "name", e.getName() != null ? e.getName() : "",
                "triggerType", e.getTriggerType() != null ? e.getTriggerType() : "",
                "amount", e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO,
                "narration", e.getNarration() != null ? e.getNarration() : "",
                "dayOfMonth", e.getDayOfMonth() != null ? e.getDayOfMonth() : 0,
                "nextRunDate", e.getNextRunDate() != null ? e.getNextRunDate().toString() : "",
                "lastRunDate", e.getLastRunDate() != null ? e.getLastRunDate().toString() : "",
                "active", e.isActive()
        );
    }
}

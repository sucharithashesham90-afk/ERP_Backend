package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.ExchangeRate;
import com.erp.platform.modules.accounting.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExchangeRateService {

    private final ExchangeRateRepository repo;
    private final TenantContext tenantContext;

    public PageResponse<ExchangeRate> list(Pageable pageable) {
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    public ExchangeRate getById(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Exchange rate not found: " + id));
    }

    public BigDecimal getRate(String base, String target, LocalDate asOf) {
        if (base.equals(target)) return BigDecimal.ONE;
        var page = repo.findLatestRate(tenantContext.current(), base, target, asOf,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "effectiveDate")));
        return page.getContent().stream().findFirst()
                .map(ExchangeRate::getRate)
                .orElse(BigDecimal.ONE);
    }

    @Transactional
    public ExchangeRate create(Map<String, Object> body) {
        ExchangeRate er = new ExchangeRate();
        er.setTenantId(tenantContext.current());
        apply(er, body);
        return repo.save(er);
    }

    @Transactional
    public ExchangeRate update(UUID id, Map<String, Object> body) {
        ExchangeRate er = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Exchange rate not found: " + id));
        apply(er, body);
        return repo.save(er);
    }

    @Transactional
    public void delete(UUID id) {
        ExchangeRate er = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Exchange rate not found: " + id));
        er.setDeletedAt(LocalDateTime.now());
        repo.save(er);
    }

    private void apply(ExchangeRate er, Map<String, Object> body) {
        if (body.containsKey("baseCurrency")) er.setBaseCurrency(body.get("baseCurrency").toString());
        if (body.containsKey("targetCurrency")) er.setTargetCurrency(body.get("targetCurrency").toString());
        if (body.containsKey("rate")) er.setRate(new BigDecimal(body.get("rate").toString()));
        if (body.containsKey("effectiveDate")) er.setEffectiveDate(LocalDate.parse(body.get("effectiveDate").toString()));
        if (body.containsKey("active")) er.setActive(Boolean.parseBoolean(body.get("active").toString()));
        if (body.containsKey("notes")) er.setNotes(body.get("notes") != null ? body.get("notes").toString() : null);
    }
}

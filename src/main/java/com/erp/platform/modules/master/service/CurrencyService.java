package com.erp.platform.modules.master.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.entity.Currency;
import com.erp.platform.modules.master.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final TenantContext tenantContext;

    public PageResponse<Map<String, Object>> list(Pageable pageable) {
        return PageResponse.of(currencyRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toMap));
    }

    public List<Map<String, Object>> listAll() {
        return currencyRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current()).stream().map(this::toMap).toList();
    }

    public Map<String, Object> getById(UUID id) {
        return toMap(currencyRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Currency not found: " + id)));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> req) {
        Currency e = new Currency();
        e.setTenantId(tenantContext.current());
        e.setCode((String) req.get("code"));
        e.setName((String) req.get("name"));
        e.setSymbol((String) req.get("symbol"));
        if (req.get("decimalPlaces") != null) e.setDecimalPlaces(Integer.parseInt(req.get("decimalPlaces").toString()));
        if (req.get("exchangeRate") != null) e.setExchangeRate(new BigDecimal(req.get("exchangeRate").toString()));
        e.setBaseCurrency(Boolean.TRUE.equals(req.get("baseCurrency")));
        e.setDescription((String) req.get("description"));
        e.setActive(req.get("active") == null || Boolean.TRUE.equals(req.get("active")));
        return toMap(currencyRepository.save(e));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> req) {
        Currency e = currencyRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Currency not found: " + id));
        e.setCode((String) req.get("code"));
        e.setName((String) req.get("name"));
        e.setSymbol((String) req.get("symbol"));
        if (req.get("decimalPlaces") != null) e.setDecimalPlaces(Integer.parseInt(req.get("decimalPlaces").toString()));
        if (req.get("exchangeRate") != null) e.setExchangeRate(new BigDecimal(req.get("exchangeRate").toString()));
        e.setBaseCurrency(Boolean.TRUE.equals(req.get("baseCurrency")));
        if (req.containsKey("description")) e.setDescription((String) req.get("description"));
        if (req.get("active") != null) e.setActive(Boolean.TRUE.equals(req.get("active")));
        return toMap(currencyRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        Currency e = currencyRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Currency not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        currencyRepository.save(e);
    }

    private Map<String, Object> toMap(Currency e) {
        return Map.of(
                "id", e.getId(), "code", e.getCode() != null ? e.getCode() : "",
                "name", e.getName() != null ? e.getName() : "",
                "symbol", e.getSymbol() != null ? e.getSymbol() : "",
                "decimalPlaces", e.getDecimalPlaces(),
                "exchangeRate", e.getExchangeRate() != null ? e.getExchangeRate() : BigDecimal.ONE,
                "baseCurrency", e.isBaseCurrency(),
                "description", e.getDescription() != null ? e.getDescription() : "",
                "active", e.isActive()
        );
    }
}

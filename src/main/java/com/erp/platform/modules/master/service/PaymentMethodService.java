package com.erp.platform.modules.master.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.entity.PaymentMethod;
import com.erp.platform.modules.master.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final TenantContext tenantContext;

    public PageResponse<Map<String, Object>> list(Pageable pageable) {
        return PageResponse.of(paymentMethodRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toMap));
    }

    public List<Map<String, Object>> listAll() {
        return paymentMethodRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current()).stream().map(this::toMap).toList();
    }

    public Map<String, Object> getById(UUID id) {
        return toMap(paymentMethodRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("PaymentMethod not found: " + id)));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> req) {
        PaymentMethod e = new PaymentMethod();
        e.setTenantId(tenantContext.current());
        e.setName((String) req.get("name"));
        e.setCode((String) req.get("code"));
        e.setType((String) req.get("type"));
        e.setDescription((String) req.get("description"));
        e.setBankRequired(Boolean.TRUE.equals(req.get("bankRequired")));
        e.setReferenceRequired(Boolean.TRUE.equals(req.get("referenceRequired")));
        e.setActive(req.get("active") == null || Boolean.TRUE.equals(req.get("active")));
        return toMap(paymentMethodRepository.save(e));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> req) {
        PaymentMethod e = paymentMethodRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("PaymentMethod not found: " + id));
        e.setName((String) req.get("name"));
        e.setCode((String) req.get("code"));
        e.setType((String) req.get("type"));
        e.setDescription((String) req.get("description"));
        e.setBankRequired(Boolean.TRUE.equals(req.get("bankRequired")));
        e.setReferenceRequired(Boolean.TRUE.equals(req.get("referenceRequired")));
        if (req.get("active") != null) e.setActive(Boolean.TRUE.equals(req.get("active")));
        return toMap(paymentMethodRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        PaymentMethod e = paymentMethodRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("PaymentMethod not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        paymentMethodRepository.save(e);
    }

    private Map<String, Object> toMap(PaymentMethod e) {
        return Map.of(
                "id", e.getId(), "name", e.getName() != null ? e.getName() : "",
                "code", e.getCode() != null ? e.getCode() : "",
                "type", e.getType() != null ? e.getType() : "",
                "description", e.getDescription() != null ? e.getDescription() : "",
                "bankRequired", e.isBankRequired(),
                "referenceRequired", e.isReferenceRequired(), "active", e.isActive()
        );
    }
}

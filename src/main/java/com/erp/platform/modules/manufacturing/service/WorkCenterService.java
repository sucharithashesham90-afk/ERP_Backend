package com.erp.platform.modules.manufacturing.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.manufacturing.entity.WorkCenter;
import com.erp.platform.modules.manufacturing.repository.WorkCenterRepository;
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
@Transactional(readOnly = true)
public class WorkCenterService {

    private final WorkCenterRepository repo;
    private final TenantContext tenantContext;

    public PageResponse<WorkCenter> list(Pageable pageable) {
        return PageResponse.of(repo.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable));
    }

    public List<WorkCenter> listActive() {
        return repo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current());
    }

    public WorkCenter getById(UUID id) {
        return repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Work center not found: " + id));
    }

    @Transactional
    public WorkCenter create(Map<String, Object> body) {
        WorkCenter wc = new WorkCenter();
        wc.setTenantId(tenantContext.current());
        apply(wc, body);
        return repo.save(wc);
    }

    @Transactional
    public WorkCenter update(UUID id, Map<String, Object> body) {
        WorkCenter wc = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Work center not found: " + id));
        apply(wc, body);
        return repo.save(wc);
    }

    @Transactional
    public void delete(UUID id) {
        WorkCenter wc = repo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Work center not found: " + id));
        wc.setDeletedAt(LocalDateTime.now());
        repo.save(wc);
    }

    private void apply(WorkCenter wc, Map<String, Object> body) {
        if (body.containsKey("code")) wc.setCode(s(body, "code"));
        if (body.containsKey("name")) wc.setName(s(body, "name"));
        if (body.containsKey("type")) wc.setType(s(body, "type"));
        if (body.containsKey("location")) wc.setLocation(s(body, "location"));
        if (body.containsKey("capacity") && body.get("capacity") != null)
            wc.setCapacity(new BigDecimal(body.get("capacity").toString()));
        if (body.containsKey("capacityUnit")) wc.setCapacityUnit(s(body, "capacityUnit"));
        if (body.containsKey("costPerHour") && body.get("costPerHour") != null)
            wc.setCostPerHour(new BigDecimal(body.get("costPerHour").toString()));
        if (body.containsKey("active")) wc.setActive(Boolean.parseBoolean(body.get("active").toString()));
        if (body.containsKey("notes")) wc.setNotes(s(body, "notes"));
    }

    private String s(Map<String, Object> body, String key) {
        Object v = body.get(key); return v != null ? v.toString() : null;
    }
}

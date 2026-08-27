package com.erp.platform.modules.hr.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.entity.Holiday;
import com.erp.platform.modules.hr.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final TenantContext tenantContext;

    public PageResponse<Map<String, Object>> list(Pageable pageable) {
        return PageResponse.of(holidayRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toMap));
    }

    public List<Map<String, Object>> listByYear(int year) {
        return holidayRepository.findByTenantIdAndYearAndDeletedAtIsNull(tenantContext.current(), year).stream().map(this::toMap).toList();
    }

    public Map<String, Object> getById(UUID id) {
        return toMap(holidayRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Holiday not found: " + id)));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> req) {
        Holiday e = new Holiday();
        e.setTenantId(tenantContext.current());
        e.setName((String) req.get("name"));
        e.setDate(LocalDate.parse((String) req.get("date")));
        e.setType((String) req.get("type"));
        e.setDescription((String) req.get("description"));
        LocalDate d = LocalDate.parse((String) req.get("date"));
        e.setYear(d.getYear());
        e.setActive(req.get("active") == null || Boolean.TRUE.equals(req.get("active")));
        return toMap(holidayRepository.save(e));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> req) {
        Holiday e = holidayRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Holiday not found: " + id));
        e.setName((String) req.get("name"));
        e.setDate(LocalDate.parse((String) req.get("date")));
        e.setType((String) req.get("type"));
        e.setDescription((String) req.get("description"));
        LocalDate d = LocalDate.parse((String) req.get("date"));
        e.setYear(d.getYear());
        if (req.get("active") != null) e.setActive(Boolean.TRUE.equals(req.get("active")));
        return toMap(holidayRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        Holiday e = holidayRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("Holiday not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        holidayRepository.save(e);
    }

    private Map<String, Object> toMap(Holiday e) {
        return Map.of(
                "id", e.getId(), "name", e.getName() != null ? e.getName() : "",
                "date", e.getDate() != null ? e.getDate().toString() : "",
                "type", e.getType() != null ? e.getType() : "",
                "description", e.getDescription() != null ? e.getDescription() : "",
                "year", e.getYear(), "active", e.isActive()
        );
    }
}

package com.erp.platform.modules.hr.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.entity.WorkingDay;
import com.erp.platform.modules.hr.repository.WorkingDayRepository;
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
public class WorkingDayService {

    private final WorkingDayRepository workingDayRepository;
    private final TenantContext tenantContext;

    public PageResponse<Map<String, Object>> list(Pageable pageable) {
        return PageResponse.of(workingDayRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toMap));
    }

    public List<Map<String, Object>> listAll() {
        return workingDayRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current()).stream().map(this::toMap).toList();
    }

    public Map<String, Object> getById(UUID id) {
        return toMap(workingDayRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("WorkingDay not found: " + id)));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> req) {
        WorkingDay e = new WorkingDay();
        e.setTenantId(tenantContext.current());
        e.setShiftName((String) req.get("shiftName"));
        e.setDayOfWeek((String) req.get("dayOfWeek"));
        e.setWorking(req.get("working") == null || Boolean.TRUE.equals(req.get("working")));
        if (req.get("workHours") != null) e.setWorkHours(Double.parseDouble(req.get("workHours").toString()));
        e.setStartTime((String) req.get("startTime"));
        e.setEndTime((String) req.get("endTime"));
        if (req.get("breakMinutes") != null) e.setBreakMinutes(Integer.parseInt(req.get("breakMinutes").toString()));
        e.setActive(req.get("active") == null || Boolean.TRUE.equals(req.get("active")));
        return toMap(workingDayRepository.save(e));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> req) {
        WorkingDay e = workingDayRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("WorkingDay not found: " + id));
        e.setShiftName((String) req.get("shiftName"));
        e.setDayOfWeek((String) req.get("dayOfWeek"));
        e.setWorking(req.get("working") == null || Boolean.TRUE.equals(req.get("working")));
        if (req.get("workHours") != null) e.setWorkHours(Double.parseDouble(req.get("workHours").toString()));
        e.setStartTime((String) req.get("startTime"));
        e.setEndTime((String) req.get("endTime"));
        if (req.get("breakMinutes") != null) e.setBreakMinutes(Integer.parseInt(req.get("breakMinutes").toString()));
        if (req.get("active") != null) e.setActive(Boolean.TRUE.equals(req.get("active")));
        return toMap(workingDayRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        WorkingDay e = workingDayRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("WorkingDay not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        workingDayRepository.save(e);
    }

    private Map<String, Object> toMap(WorkingDay e) {
        return Map.of(
                "id", e.getId(), "shiftName", e.getShiftName() != null ? e.getShiftName() : "",
                "dayOfWeek", e.getDayOfWeek() != null ? e.getDayOfWeek() : "",
                "working", e.isWorking(), "workHours", e.getWorkHours() != null ? e.getWorkHours() : 8.0,
                "startTime", e.getStartTime() != null ? e.getStartTime() : "",
                "endTime", e.getEndTime() != null ? e.getEndTime() : "",
                "breakMinutes", e.getBreakMinutes(), "active", e.isActive()
        );
    }
}

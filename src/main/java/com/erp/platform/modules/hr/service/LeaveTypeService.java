package com.erp.platform.modules.hr.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.hr.entity.LeaveType;
import com.erp.platform.modules.hr.repository.LeaveTypeRepository;
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
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final TenantContext tenantContext;

    public PageResponse<Map<String, Object>> list(Pageable pageable) {
        return PageResponse.of(leaveTypeRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toMap));
    }

    public List<Map<String, Object>> listAll() {
        return leaveTypeRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current()).stream().map(this::toMap).toList();
    }

    public Map<String, Object> getById(UUID id) {
        return toMap(leaveTypeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("LeaveType not found: " + id)));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> req) {
        LeaveType e = new LeaveType();
        e.setTenantId(tenantContext.current());
        e.setName((String) req.get("name"));
        e.setCode((String) req.get("code"));
        if (req.get("daysAllowed") != null) e.setDaysAllowed(Integer.parseInt(req.get("daysAllowed").toString()));
        e.setCarryForward(Boolean.TRUE.equals(req.get("carryForward")));
        e.setPaid(req.get("paid") == null || Boolean.TRUE.equals(req.get("paid")));
        e.setActive(req.get("active") == null || Boolean.TRUE.equals(req.get("active")));
        return toMap(leaveTypeRepository.save(e));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> req) {
        LeaveType e = leaveTypeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("LeaveType not found: " + id));
        e.setName((String) req.get("name"));
        e.setCode((String) req.get("code"));
        if (req.get("daysAllowed") != null) e.setDaysAllowed(Integer.parseInt(req.get("daysAllowed").toString()));
        e.setCarryForward(Boolean.TRUE.equals(req.get("carryForward")));
        e.setPaid(req.get("paid") == null || Boolean.TRUE.equals(req.get("paid")));
        if (req.get("active") != null) e.setActive(Boolean.TRUE.equals(req.get("active")));
        return toMap(leaveTypeRepository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        LeaveType e = leaveTypeRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> new RuntimeException("LeaveType not found: " + id));
        e.setDeletedAt(LocalDateTime.now());
        leaveTypeRepository.save(e);
    }

    private Map<String, Object> toMap(LeaveType e) {
        return Map.of(
                "id", e.getId(), "name", e.getName() != null ? e.getName() : "",
                "code", e.getCode() != null ? e.getCode() : "",
                "daysAllowed", e.getDaysAllowed(),
                "carryForward", e.isCarryForward(), "paid", e.isPaid(), "active", e.isActive()
        );
    }
}

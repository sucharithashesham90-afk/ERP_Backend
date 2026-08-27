package com.erp.platform.modules.crm.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.crm.entity.Activity;
import com.erp.platform.modules.crm.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final TenantContext tenantContext;

    public PageResponse<Activity> list(UUID referenceId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        if (referenceId != null) {
            return PageResponse.of(activityRepository.findByTenantIdAndReferenceIdAndDeletedAtIsNull(tenantId, referenceId, pageable));
        }
        return PageResponse.of(activityRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public Activity getById(UUID id) {
        return activityRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Activity not found: " + id));
    }

    @Transactional
    public Activity create(Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        Activity activity = new Activity();
        activity.setTenantId(tenantId);
        apply(activity, body);
        if (activity.getStatus() == null) activity.setStatus("PLANNED");
        return activityRepository.save(activity);
    }

    @Transactional
    public Activity update(UUID id, Map<String, Object> body) {
        Activity activity = activityRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Activity not found: " + id));
        apply(activity, body);
        return activityRepository.save(activity);
    }

    @Transactional
    public Activity complete(UUID id) {
        Activity activity = activityRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Activity not found: " + id));
        activity.setStatus("COMPLETED");
        activity.setCompletedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }

    @Transactional
    public void delete(UUID id) {
        Activity activity = activityRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Activity not found: " + id));
        activity.setDeletedAt(LocalDateTime.now());
        activityRepository.save(activity);
    }

    private void apply(Activity a, Map<String, Object> body) {
        if (body.containsKey("type")) a.setType(str(body, "type"));
        if (body.containsKey("subject")) a.setSubject(str(body, "subject"));
        if (body.containsKey("description")) a.setDescription(str(body, "description"));
        if (body.containsKey("status")) a.setStatus(str(body, "status"));
        if (body.containsKey("referenceType")) a.setReferenceType(str(body, "referenceType"));
        if (body.containsKey("referenceId") && body.get("referenceId") != null)
            a.setReferenceId(UUID.fromString(body.get("referenceId").toString()));
        if (body.containsKey("assignedTo") && body.get("assignedTo") != null)
            a.setAssignedTo(UUID.fromString(body.get("assignedTo").toString()));
        if (body.containsKey("scheduledAt") && body.get("scheduledAt") != null)
            a.setScheduledAt(LocalDateTime.parse(body.get("scheduledAt").toString()));
    }

    private String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v != null ? v.toString() : null;
    }
}

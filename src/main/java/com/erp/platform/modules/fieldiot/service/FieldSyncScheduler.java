package com.erp.platform.modules.fieldiot.service;

import com.erp.platform.modules.fieldiot.repository.FieldPlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Pulls weather, soil and satellite observations on a schedule.
 *
 * <p>Sync was reachable only by pressing a button, so a dashboard was as current as the last person
 * who happened to open the screen and remember. Field conditions are the one thing on it that
 * changes on their own, which makes a stale reading actively misleading — a soil-moisture alert
 * nobody triggered is worth nothing.
 *
 * <p>Off by default. Live mode calls an external provider once per field per run, so turning that
 * into a recurring cost is a deliberate act, not something a deployment inherits silently. Enable
 * with {@code FIELDIOT_SYNC_ENABLED=true}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FieldSyncScheduler {

    private final FieldIotService fieldIotService;
    private final FieldPlotRepository fieldPlotRepository;

    @Value("${fieldiot.sync.enabled:false}")
    private boolean enabled;

    /**
     * Every day at 05:30, before the working day — overridable so a grower watching irrigation can
     * run it hourly without a rebuild.
     */
    @Scheduled(cron = "${fieldiot.sync.cron:0 30 5 * * *}")
    public void syncAllTenants() {
        if (!enabled) return;

        // A scheduled run has no request behind it, so there is no tenant on the context — the
        // tenant is passed to sync() explicitly. Ask the data which tenants actually have fields
        // mapped rather than sweeping every tenant in the system.
        List<UUID> tenantIds = fieldPlotRepository.findTenantIdsWithActiveFields();
        if (tenantIds.isEmpty()) return;

        int synced = 0;
        for (UUID tenantId : tenantIds) {
            try {
                synced += fieldIotService.sync(tenantId).getFieldsSynced();
            } catch (Exception e) {
                // One tenant's provider failure must not stop the rest from being refreshed.
                log.warn("Scheduled field sync failed for tenant {}: {}", tenantId, e.getMessage());
            }
        }
        log.info("Scheduled field sync complete: {} field(s) across {} tenant(s)", synced, tenantIds.size());
    }
}

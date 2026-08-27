package com.erp.platform.modules.reports.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.reports.dto.CreateReportDefinitionRequest;
import com.erp.platform.modules.reports.dto.CreateReportScheduleRequest;
import com.erp.platform.modules.reports.dto.ReportDefinitionDto;
import com.erp.platform.modules.reports.dto.ReportRunDto;
import com.erp.platform.modules.reports.dto.ReportScheduleDto;
import com.erp.platform.modules.reports.entity.ReportDefinition;
import com.erp.platform.modules.reports.entity.ReportDefinition.ReportCategory;
import com.erp.platform.modules.reports.entity.ReportRun;
import com.erp.platform.modules.reports.entity.ReportRun.RunStatus;
import com.erp.platform.modules.reports.entity.ReportSchedule;
import com.erp.platform.modules.reports.repository.ReportDefinitionRepository;
import com.erp.platform.modules.reports.repository.ReportRunRepository;
import com.erp.platform.modules.reports.repository.ReportScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportDefinitionService {

    private final ReportDefinitionRepository reportDefinitionRepository;
    private final ReportScheduleRepository reportScheduleRepository;
    private final ReportRunRepository reportRunRepository;
    private final TenantContext tenantContext;

    public PageResponse<ReportDefinitionDto> list(ReportCategory category, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Page<ReportDefinition> page = category != null
                ? reportDefinitionRepository.findByTenantIdAndReportCategoryAndDeletedAtIsNull(tenantId, category, pageable)
                : reportDefinitionRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public ReportDefinitionDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public ReportDefinitionDto create(CreateReportDefinitionRequest request) {
        UUID tenantId = tenantContext.current();
        ReportDefinition report = new ReportDefinition();
        report.setTenantId(tenantId);
        report.setReportCode(generateReportCode(tenantId));
        mapRequestToEntity(request, report);
        ReportDefinition saved = reportDefinitionRepository.save(report);
        log.info("ReportDefinition created: id={}, code={}", saved.getId(), saved.getReportCode());
        return toDto(saved);
    }

    @Transactional
    public ReportDefinitionDto update(UUID id, CreateReportDefinitionRequest request) {
        ReportDefinition report = findOrThrow(id);
        mapRequestToEntity(request, report);
        return toDto(reportDefinitionRepository.save(report));
    }

    @Transactional
    public ReportDefinitionDto duplicate(UUID id) {
        UUID tenantId = tenantContext.current();
        ReportDefinition original = findOrThrow(id);
        ReportDefinition copy = new ReportDefinition();
        copy.setTenantId(tenantId);
        copy.setReportCode(generateReportCode(tenantId));
        copy.setName(original.getName() + " (Copy)");
        copy.setDescription(original.getDescription());
        copy.setReportCategory(original.getReportCategory());
        copy.setBaseEntity(original.getBaseEntity());
        copy.setColumns(original.getColumns());
        copy.setDefaultFilters(original.getDefaultFilters());
        copy.setDefaultSortField(original.getDefaultSortField());
        copy.setDefaultSortDirection(original.getDefaultSortDirection());
        copy.setDefaultPageSize(original.getDefaultPageSize());
        copy.setGroupByField(original.getGroupByField());
        copy.setAggregations(original.getAggregations());
        copy.setPublic(false);
        copy.setCreatedBy(original.getCreatedBy());
        copy.setActive(true);
        copy.setNotes(original.getNotes());
        ReportDefinition saved = reportDefinitionRepository.save(copy);
        log.info("ReportDefinition duplicated: original={}, copy={}", id, saved.getId());
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        ReportDefinition report = findOrThrow(id);
        report.setDeletedAt(LocalDateTime.now());
        reportDefinitionRepository.save(report);
        log.info("ReportDefinition soft-deleted: id={}", id);
    }

    @Transactional
    public ReportScheduleDto addSchedule(UUID reportId, CreateReportScheduleRequest request) {
        UUID tenantId = tenantContext.current();
        ReportDefinition report = findOrThrow(reportId);
        ReportSchedule schedule = new ReportSchedule();
        schedule.setTenantId(tenantId);
        schedule.setReportDefinition(report);
        schedule.setScheduleName(request.getScheduleName());
        schedule.setFrequency(request.getFrequency());
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setDayOfMonth(request.getDayOfMonth());
        schedule.setHour(request.getHour());
        schedule.setRecipients(request.getRecipients());
        schedule.setFormat(request.getFormat() != null ? request.getFormat() : "CSV");
        schedule.setActive(request.isActive());
        schedule.setNextRunAt(request.getNextRunAt());
        ReportSchedule saved = reportScheduleRepository.save(schedule);
        log.info("ReportSchedule added: reportId={}, scheduleId={}", reportId, saved.getId());
        return toScheduleDto(saved);
    }

    @Transactional
    public void recordRun(UUID reportId, String runBy, String filtersJson, int recordCount,
                          long durationMs, boolean success, String error) {
        UUID tenantId = tenantContext.current();
        ReportDefinition report = findOrThrow(reportId);
        ReportRun run = new ReportRun();
        run.setTenantId(tenantId);
        run.setReportDefinition(report);
        run.setRunBy(runBy);
        run.setRunAt(LocalDateTime.now());
        run.setFilters(filtersJson);
        run.setRecordCount(recordCount);
        run.setDurationMs(durationMs);
        run.setStatus(success ? RunStatus.SUCCESS : RunStatus.FAILED);
        run.setErrorMessage(error);
        reportRunRepository.save(run);

        report.setLastRunAt(run.getRunAt());
        report.setRunCount(report.getRunCount() + 1);
        reportDefinitionRepository.save(report);
    }

    public PageResponse<ReportRunDto> listRuns(UUID reportId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        findOrThrow(reportId); // validate access
        Page<ReportRun> page = reportRunRepository
                .findByTenantIdAndReportDefinition_IdAndDeletedAtIsNull(tenantId, reportId, pageable);
        return PageResponse.of(page.map(this::toRunDto));
    }

    private void mapRequestToEntity(CreateReportDefinitionRequest request, ReportDefinition report) {
        report.setName(request.getName());
        report.setDescription(request.getDescription());
        report.setReportCategory(request.getReportCategory());
        report.setBaseEntity(request.getBaseEntity());
        report.setColumns(request.getColumns());
        report.setDefaultFilters(request.getDefaultFilters());
        report.setDefaultSortField(request.getDefaultSortField());
        report.setDefaultSortDirection(request.getDefaultSortDirection() != null ? request.getDefaultSortDirection() : "DESC");
        report.setDefaultPageSize(request.getDefaultPageSize() > 0 ? request.getDefaultPageSize() : 20);
        report.setGroupByField(request.getGroupByField());
        report.setAggregations(request.getAggregations());
        report.setPublic(request.isPublic());
        report.setCreatedBy(request.getCreatedBy());
        report.setActive(request.isActive());
        report.setNotes(request.getNotes());
    }

    private ReportDefinition findOrThrow(UUID id) {
        return reportDefinitionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Report definition not found: " + id));
    }

    private String generateReportCode(UUID tenantId) {
        long count = reportDefinitionRepository.countByTenantId(tenantId);
        return String.format("RPT-%d-%03d", Year.now().getValue(), count + 1);
    }

    private ReportDefinitionDto toDto(ReportDefinition r) {
        ReportDefinitionDto dto = new ReportDefinitionDto();
        dto.setId(r.getId());
        dto.setReportCode(r.getReportCode());
        dto.setName(r.getName());
        dto.setDescription(r.getDescription());
        dto.setReportCategory(r.getReportCategory());
        dto.setBaseEntity(r.getBaseEntity());
        dto.setColumns(r.getColumns());
        dto.setDefaultFilters(r.getDefaultFilters());
        dto.setDefaultSortField(r.getDefaultSortField());
        dto.setDefaultSortDirection(r.getDefaultSortDirection());
        dto.setDefaultPageSize(r.getDefaultPageSize());
        dto.setGroupByField(r.getGroupByField());
        dto.setAggregations(r.getAggregations());
        dto.setPublic(r.isPublic());
        dto.setCreatedBy(r.getCreatedBy());
        dto.setLastRunAt(r.getLastRunAt());
        dto.setRunCount(r.getRunCount());
        dto.setActive(r.isActive());
        dto.setNotes(r.getNotes());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    private ReportScheduleDto toScheduleDto(ReportSchedule s) {
        ReportScheduleDto dto = new ReportScheduleDto();
        dto.setId(s.getId());
        dto.setReportDefinitionId(s.getReportDefinition().getId());
        dto.setScheduleName(s.getScheduleName());
        dto.setFrequency(s.getFrequency());
        dto.setDayOfWeek(s.getDayOfWeek());
        dto.setDayOfMonth(s.getDayOfMonth());
        dto.setHour(s.getHour());
        dto.setRecipients(s.getRecipients());
        dto.setFormat(s.getFormat());
        dto.setActive(s.isActive());
        dto.setLastRunAt(s.getLastRunAt());
        dto.setNextRunAt(s.getNextRunAt());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }

    private ReportRunDto toRunDto(ReportRun r) {
        ReportRunDto dto = new ReportRunDto();
        dto.setId(r.getId());
        dto.setReportDefinitionId(r.getReportDefinition().getId());
        dto.setRunBy(r.getRunBy());
        dto.setRunAt(r.getRunAt());
        dto.setFilters(r.getFilters());
        dto.setRecordCount(r.getRecordCount());
        dto.setDurationMs(r.getDurationMs());
        dto.setStatus(r.getStatus());
        dto.setErrorMessage(r.getErrorMessage());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}

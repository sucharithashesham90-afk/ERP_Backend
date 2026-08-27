package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.Season;
import com.erp.platform.modules.master.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final TenantContext tenantContext;

    public PageResponse<Season> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(seasonRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable));
    }

    public Season getById(UUID id) {
        return findOrThrow(id);
    }

    @Transactional
    public Season create(Season req) {
        req.setTenantId(tenantContext.current());
        Season saved = seasonRepository.save(req);
        log.info("Season created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Season update(UUID id, Season req) {
        Season season = findOrThrow(id);
        season.setName(req.getName());
        season.setCode(req.getCode());
        season.setStartDate(req.getStartDate());
        season.setEndDate(req.getEndDate());
        season.setDescription(req.getDescription());
        season.setActive(req.isActive());
        return seasonRepository.save(season);
    }

    @Transactional
    public void delete(UUID id) {
        Season season = findOrThrow(id);
        season.setDeletedAt(LocalDateTime.now());
        seasonRepository.save(season);
        log.info("Season deleted: id={}", id);
    }

    private Season findOrThrow(UUID id) {
        return seasonRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Season not found: " + id));
    }
}

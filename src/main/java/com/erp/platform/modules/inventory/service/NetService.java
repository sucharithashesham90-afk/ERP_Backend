package com.erp.platform.modules.inventory.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.inventory.dto.NetDto;
import com.erp.platform.modules.inventory.entity.Net;
import com.erp.platform.modules.inventory.repository.GodownRepository;
import com.erp.platform.modules.inventory.repository.NetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NetService {

    private final NetRepository netRepository;
    private final GodownRepository godownRepository;
    private final TenantContext tenantContext;

    public PageResponse<NetDto> list(UUID godownId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = godownId != null
                ? netRepository.findByTenantIdAndGodownIdAndDeletedAtIsNull(tenantId, godownId, pageable)
                : netRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    @Transactional
    public NetDto create(NetDto req) {
        UUID tenantId = tenantContext.current();
        validate(req);
        Net n = new Net();
        n.setTenantId(tenantId);
        apply(n, req, tenantId);
        n = netRepository.save(n);
        log.info("Net created: id={}, name={}", n.getId(), n.getName());
        return toDto(n);
    }

    @Transactional
    public NetDto update(UUID id, NetDto req) {
        UUID tenantId = tenantContext.current();
        validate(req);
        Net n = netRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Net not found: " + id));
        apply(n, req, tenantId);
        n = netRepository.save(n);
        log.info("Net updated: id={}", id);
        return toDto(n);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        Net n = netRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Net not found: " + id));
        n.setDeletedAt(java.time.LocalDateTime.now());
        netRepository.save(n);
        log.info("Net deleted: id={}", id);
    }

    private void validate(NetDto req) {
        if (!StringUtils.hasText(req.getName())) throw AppException.badRequest("Net name is required");
        if (!StringUtils.hasText(req.getLocation())) throw AppException.badRequest("Location is required");
        if (req.getGodownId() == null) throw AppException.badRequest("Godown is required");
    }

    private void apply(Net n, NetDto req, UUID tenantId) {
        n.setName(req.getName().trim());
        n.setLocation(req.getLocation());
        n.setGodownId(req.getGodownId());
        n.setGodownName(resolveGodownName(tenantId, req.getGodownId(), req.getGodownName()));
        n.setLandmark(req.getLandmark());
        n.setDimension(req.getDimension());
        n.setPositionInGodown(req.getPositionInGodown());
    }

    private String resolveGodownName(UUID tenantId, UUID godownId, String fallback) {
        if (godownId == null) return fallback;
        return godownRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, godownId)
                .map(g -> g.getName())
                .orElse(fallback);
    }

    private NetDto toDto(Net n) {
        NetDto d = new NetDto();
        d.setId(n.getId());
        d.setName(n.getName());
        d.setLocation(n.getLocation());
        d.setGodownId(n.getGodownId());
        d.setGodownName(n.getGodownName());
        d.setLandmark(n.getLandmark());
        d.setDimension(n.getDimension());
        d.setPositionInGodown(n.getPositionInGodown());
        d.setActive(n.isActive());
        d.setCreatedAt(n.getCreatedAt());
        return d;
    }
}

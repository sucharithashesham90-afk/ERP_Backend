package com.erp.platform.modules.agri.service;
import java.util.UUID;

import com.erp.platform.modules.agri.dto.CreateGrowerPlotRequest;
import com.erp.platform.modules.agri.dto.GrowerPlotDto;
import com.erp.platform.modules.agri.entity.GrowerPlot;
import com.erp.platform.modules.agri.repository.GrowerPlotRepository;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GrowerPlotService {

    private final GrowerPlotRepository repository;
    private final TenantContext tenantContext;

    public PageResponse<GrowerPlotDto> list(int page, int size) {
        UUID tid = tenantContext.current();
        Page<GrowerPlot> p = repository.findByTenantIdAndDeletedAtIsNull(
                tid, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(p.map(this::toDto));
    }

    public GrowerPlotDto create(CreateGrowerPlotRequest req) {
        GrowerPlot e = new GrowerPlot();
        e.setTenantId(tenantContext.current());
        e.setPlotCode(req.plotCode());
        e.setGrowerCode(req.growerCode());
        e.setGrowerName(req.growerName());
        e.setVillage(req.village());
        e.setSurveyNumber(req.surveyNumber());
        e.setPlotAreaAcres(req.plotAreaAcres());
        e.setSoilType(req.soilType());
        e.setIrrigationType(req.irrigationType());
        e.setCropName(req.cropName());
        e.setSeason(req.season());
        e.setActive(req.active() != null ? req.active() : true);
        return toDto(repository.save(e));
    }

    public GrowerPlotDto update(UUID id, CreateGrowerPlotRequest req) {
        GrowerPlot e = repository.findById(id).orElseThrow();
        e.setPlotCode(req.plotCode());
        e.setGrowerCode(req.growerCode());
        e.setGrowerName(req.growerName());
        e.setVillage(req.village());
        e.setSurveyNumber(req.surveyNumber());
        e.setPlotAreaAcres(req.plotAreaAcres());
        e.setSoilType(req.soilType());
        e.setIrrigationType(req.irrigationType());
        e.setCropName(req.cropName());
        e.setSeason(req.season());
        e.setActive(req.active());
        return toDto(repository.save(e));
    }

    public void delete(UUID id) {
        GrowerPlot e = repository.findById(id).orElseThrow();
        e.setDeletedAt(LocalDateTime.now());
        repository.save(e);
    }

    private GrowerPlotDto toDto(GrowerPlot e) {
        return new GrowerPlotDto(
                e.getId(), e.getPlotCode(), e.getGrowerCode(), e.getGrowerName(),
                e.getVillage(), e.getSurveyNumber(), e.getPlotAreaAcres(),
                e.getSoilType(), e.getIrrigationType(), e.getCropName(),
                e.getSeason(), e.getActive());
    }
}


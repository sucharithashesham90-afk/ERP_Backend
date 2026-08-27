package com.erp.platform.modules.agri.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.agri.dto.CreatePlantVariantRequest;
import com.erp.platform.modules.agri.dto.PlantVariantDto;
import com.erp.platform.modules.agri.entity.PlantCategory;
import com.erp.platform.modules.agri.entity.PlantVariant;
import com.erp.platform.modules.agri.repository.PlantCategoryRepository;
import com.erp.platform.modules.agri.repository.PlantVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PlantVariantService {

    private final PlantVariantRepository repository;
    private final PlantCategoryRepository categoryRepository;
    private final TenantContext tenantContext;

    public PageResponse<PlantVariantDto> list(Pageable pageable) {
        return list(null, null, pageable);
    }

    /** List varieties, optionally narrowed to a crop and/or crop group (drives the cascade). */
    public PageResponse<PlantVariantDto> list(UUID cropId, UUID cropGroupId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<PlantVariant> all = repository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId).stream()
                .filter(v -> cropId == null || cropId.equals(v.getCropId()))
                .filter(v -> cropGroupId == null || cropGroupId.equals(v.getCropGroupId()))
                .toList();
        List<PlantVariantDto> dtos = all.stream().map(this::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<PlantVariantDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public List<PlantVariantDto> listByCategory(UUID categoryId) {
        return repository.findByTenantIdAndPlantCategoryIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current(), categoryId)
                .stream().map(this::toDto).toList();
    }

    public List<PlantVariantDto> listAll() {
        return repository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantContext.current())
                .stream().map(this::toDto).toList();
    }

    public PlantVariantDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public PlantVariantDto create(CreatePlantVariantRequest req) {
        UUID tenantId = tenantContext.current();
        PlantVariant entity = new PlantVariant();
        entity.setTenantId(tenantId);
        applyRequest(entity, req, tenantId);
        entity = repository.save(entity);
        log.info("PlantVariant created: {}", entity.getId());
        return toDto(entity);
    }

    @Transactional
    public PlantVariantDto update(UUID id, CreatePlantVariantRequest req) {
        UUID tenantId = tenantContext.current();
        PlantVariant entity = findOrThrow(id);
        applyRequest(entity, req, tenantId);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PlantVariant entity = findOrThrow(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    private void applyRequest(PlantVariant e, CreatePlantVariantRequest r, UUID tenantId) {
        // Tab 1 — Basic
        e.setName(r.getName());
        e.setAliasName(r.getAliasName());
        e.setCode(r.getCode());
        e.setActive(r.isActive());
        e.setCropGroupId(r.getCropGroupId());
        e.setCropGroupName(r.getCropGroupName());
        e.setCropId(r.getCropId());
        e.setCropName(r.getCropName());
        e.setSeedClassId(r.getSeedClassId());
        e.setSeedClassName(r.getSeedClassName());
        e.setSeedCategoryId(r.getSeedCategoryId());
        e.setSeedCategoryName(r.getSeedCategoryName());
        e.setYearOfRelease(r.getYearOfRelease());
        e.setReleaseYear(r.getReleaseYear());
        e.setNotificationNumber(r.getNotificationNumber());
        e.setDivision(r.getDivision());
        if (r.getPlantCategoryId() != null) {
            PlantCategory cat = categoryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, r.getPlantCategoryId()).orElse(null);
            e.setPlantCategory(cat);
        } else {
            e.setPlantCategory(null);
        }

        // Tab 2 — Variety Details
        e.setHybrid(r.isHybrid());
        e.setTransgenic(r.isTransgenic());
        e.setProductWiseSalesPlan(r.isProductWiseSalesPlan());
        e.setGotRequired(r.getGotRequired());
        e.setAverageSeedWeight(r.getAverageSeedWeight());
        e.setPlantProductWeight(r.getPlantProductWeight());
        e.setMaxProcessLoss(r.getMaxProcessLoss());
        e.setMinProcessLoss(r.getMinProcessLoss());
        e.setInWtPerMoisture(r.getInWtPerMoisture());
        e.setSowHarvestPrd(r.getSowHarvestPrd());
        e.setIsolationDist(r.getIsolationDist());
        e.setMaxOffTypesCount(r.getMaxOffTypesCount());
        e.setMaxSelfsCount(r.getMaxSelfsCount());
        e.setScreens(r.getScreens());
        e.setExpirationPeriodValue(r.getExpirationPeriodValue());
        e.setExpirationPeriodUnit(r.getExpirationPeriodUnit());
        e.setCharacteristics(r.getCharacteristics());
        e.setTreatments(r.getTreatments());

        // Tab 3 — Production & Processing
        e.setProductionAreaIds(listToCsv(r.getProductionAreaIds()));
        e.setProductionAreaNames(r.getProductionAreaNames());
        e.setProcessingPlantIds(listToCsv(r.getProcessingPlantIds()));
        e.setProcessingPlantNames(r.getProcessingPlantNames());

        // Legacy
        e.setDescription(r.getDescription());
        e.setLicensingAuthority(r.getLicensingAuthority());
        e.setStandardViabilityRate(r.getStandardViabilityRate());
        e.setStandardQualityGrade(r.getStandardQualityGrade());
        e.setStandardMoistureLevel(r.getStandardMoistureLevel());
        e.setProductionSeason(r.getProductionSeason());
        e.setMaturityDays(r.getMaturityDays());
    }

    private PlantVariant findOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Plant variant not found: " + id));
    }

    private PlantVariantDto toDto(PlantVariant e) {
        PlantVariantDto dto = new PlantVariantDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setAliasName(e.getAliasName());
        dto.setCode(e.getCode());
        dto.setActive(e.isActive());

        // Tab 1
        dto.setCropGroupId(e.getCropGroupId());
        dto.setCropGroupName(e.getCropGroupName());
        dto.setCropId(e.getCropId());
        dto.setCropName(e.getCropName());
        if (e.getPlantCategory() != null) {
            dto.setPlantCategoryId(e.getPlantCategory().getId());
            dto.setPlantCategoryName(e.getPlantCategory().getName());
        }
        dto.setSeedClassId(e.getSeedClassId());
        dto.setSeedClassName(e.getSeedClassName());
        dto.setSeedCategoryId(e.getSeedCategoryId());
        dto.setSeedCategoryName(e.getSeedCategoryName());
        dto.setYearOfRelease(e.getYearOfRelease());
        dto.setReleaseYear(e.getReleaseYear());
        dto.setNotificationNumber(e.getNotificationNumber());
        dto.setDivision(e.getDivision());

        // Tab 2
        dto.setHybrid(e.isHybrid());
        dto.setTransgenic(e.isTransgenic());
        dto.setProductWiseSalesPlan(e.isProductWiseSalesPlan());
        dto.setGotRequired(e.getGotRequired());
        dto.setAverageSeedWeight(e.getAverageSeedWeight());
        dto.setPlantProductWeight(e.getPlantProductWeight());
        dto.setMaxProcessLoss(e.getMaxProcessLoss());
        dto.setMinProcessLoss(e.getMinProcessLoss());
        dto.setInWtPerMoisture(e.getInWtPerMoisture());
        dto.setSowHarvestPrd(e.getSowHarvestPrd());
        dto.setIsolationDist(e.getIsolationDist());
        dto.setMaxOffTypesCount(e.getMaxOffTypesCount());
        dto.setMaxSelfsCount(e.getMaxSelfsCount());
        dto.setScreens(e.getScreens());
        dto.setExpirationPeriodValue(e.getExpirationPeriodValue());
        dto.setExpirationPeriodUnit(e.getExpirationPeriodUnit());
        dto.setCharacteristics(e.getCharacteristics());
        dto.setTreatments(e.getTreatments());

        // Tab 3
        dto.setProductionAreaIds(csvToList(e.getProductionAreaIds()));
        dto.setProductionAreaNames(e.getProductionAreaNames());
        dto.setProcessingPlantIds(csvToList(e.getProcessingPlantIds()));
        dto.setProcessingPlantNames(e.getProcessingPlantNames());

        // Legacy
        dto.setDescription(e.getDescription());
        dto.setLicensingAuthority(e.getLicensingAuthority());
        dto.setStandardViabilityRate(e.getStandardViabilityRate());
        dto.setStandardQualityGrade(e.getStandardQualityGrade());
        dto.setStandardMoistureLevel(e.getStandardMoistureLevel());
        dto.setProductionSeason(e.getProductionSeason());
        dto.setMaturityDays(e.getMaturityDays());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private String listToCsv(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    private List<String> csvToList(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.asList(csv.split(","));
    }
}

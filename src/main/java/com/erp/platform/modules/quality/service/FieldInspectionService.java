package com.erp.platform.modules.quality.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.quality.dto.FieldInspectionDto;
import com.erp.platform.modules.quality.entity.FieldInspection;
import com.erp.platform.modules.quality.entity.FieldInspectionRound;
import com.erp.platform.modules.quality.repository.FieldInspectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FieldInspectionService {

    private final FieldInspectionRepository repository;
    private final TenantContext tenantContext;

    private static final List<String> DEFAULT_ROUND_LABELS =
            List.of("First Inspection", "Second Inspection", "Third Inspection",
                    "Fourth Inspection", "Fifth Inspection");

    public PageResponse<FieldInspectionDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(
                repository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                          .map(this::toDto));
    }

    public FieldInspectionDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        return toDto(repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Field inspection not found: " + id)));
    }

    @Transactional
    public FieldInspectionDto create(Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        FieldInspection fi = new FieldInspection();
        fi.setTenantId(tenantId);
        mapHeader(fi, body);
        // pre-create 5 empty rounds
        for (int i = 1; i <= 5; i++) {
            FieldInspectionRound r = new FieldInspectionRound();
            r.setTenantId(tenantId);
            r.setInspection(fi);
            r.setRoundNumber(i);
            r.setRoundLabel(DEFAULT_ROUND_LABELS.get(i - 1));
            fi.getRounds().add(r);
        }
        FieldInspection saved = repository.save(fi);
        log.info("FieldInspection created: id={}, lot={}", saved.getId(), saved.getLotReference());
        return toDto(saved);
    }

    @Transactional
    public FieldInspectionDto updateHeader(UUID id, Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        FieldInspection fi = repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Field inspection not found: " + id));
        mapHeader(fi, body);
        return toDto(repository.save(fi));
    }

    @Transactional
    public FieldInspectionDto saveRound(UUID id, int roundNumber, Map<String, Object> body) {
        UUID tenantId = tenantContext.current();
        FieldInspection fi = repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Field inspection not found: " + id));

        FieldInspectionRound round = fi.getRounds().stream()
                .filter(r -> r.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseGet(() -> {
                    FieldInspectionRound nr = new FieldInspectionRound();
                    nr.setTenantId(tenantId);
                    nr.setInspection(fi);
                    nr.setRoundNumber(roundNumber);
                    nr.setRoundLabel(roundNumber <= 5
                            ? DEFAULT_ROUND_LABELS.get(roundNumber - 1)
                            : "Round " + roundNumber);
                    fi.getRounds().add(nr);
                    return nr;
                });

        mapRound(round, body);
        FieldInspection saved = repository.save(fi);
        log.info("FieldInspection round {} saved: inspId={}", roundNumber, id);
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        FieldInspection fi = repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> AppException.notFound("Field inspection not found: " + id));
        fi.setDeletedAt(LocalDateTime.now());
        repository.save(fi);
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void mapHeader(FieldInspection fi, Map<String, Object> b) {
        if (b.containsKey("lotReference"))      fi.setLotReference(str(b, "lotReference"));
        if (b.containsKey("jobReference"))      fi.setJobReference(str(b, "jobReference"));
        if (b.containsKey("itemName"))          fi.setItemName(str(b, "itemName"));
        if (b.containsKey("itemCode"))          fi.setItemCode(str(b, "itemCode"));
        if (b.containsKey("locationName"))      fi.setLocationName(str(b, "locationName"));
        if (b.containsKey("contractReference")) fi.setContractReference(str(b, "contractReference"));
        if (b.containsKey("inspectorName"))     fi.setInspectorName(str(b, "inspectorName"));
        if (b.containsKey("organizerName"))     fi.setOrganizerName(str(b, "organizerName"));
        if (b.containsKey("status"))            fi.setStatus(str(b, "status"));
        if (b.containsKey("notes"))             fi.setNotes(str(b, "notes"));
    }

    private void mapRound(FieldInspectionRound r, Map<String, Object> b) {
        if (b.containsKey("roundLabel"))         r.setRoundLabel(str(b, "roundLabel"));
        if (b.containsKey("inspectionDate"))     r.setInspectionDate(parseDate(b, "inspectionDate"));
        if (b.containsKey("lotCondition"))       r.setLotCondition(str(b, "lotCondition"));
        if (b.containsKey("isolationZone"))      r.setIsolationZone(parseDec(b, "isolationZone"));
        if (b.containsKey("rejectedArea"))       r.setRejectedArea(parseDec(b, "rejectedArea"));
        if (b.containsKey("recommendRejection")) r.setRecommendRejection(Boolean.TRUE.equals(b.get("recommendRejection")));
        if (b.containsKey("rejectedReasons"))    r.setRejectedReasons(str(b, "rejectedReasons"));
        if (b.containsKey("sowDate1"))           r.setSowDate1(parseDate(b, "sowDate1"));
        if (b.containsKey("sowDate2"))           r.setSowDate2(parseDate(b, "sowDate2"));
        if (b.containsKey("yieldEstimated"))     r.setYieldEstimated(parseDec(b, "yieldEstimated"));
        if (b.containsKey("yield1"))             r.setYield1(parseDec(b, "yield1"));
        if (b.containsKey("yield2"))             r.setYield2(parseDec(b, "yield2"));
        if (b.containsKey("cropStage"))              r.setCropStage(str(b, "cropStage"));
        if (b.containsKey("fieldAreaAcres"))         r.setFieldAreaAcres(parseDec(b, "fieldAreaAcres"));
        if (b.containsKey("harvestDate"))            r.setHarvestDate(parseDate(b, "harvestDate"));
        if (b.containsKey("offTypePlantCount"))      r.setOffTypePlantCount(parseInt(b, "offTypePlantCount"));
        if (b.containsKey("contaminantPlantCount"))  r.setContaminantPlantCount(parseInt(b, "contaminantPlantCount"));
        if (b.containsKey("selfPollinationCount"))   r.setSelfPollinationCount(parseInt(b, "selfPollinationCount"));
        if (b.containsKey("yieldPerAcre"))           r.setYieldPerAcre(parseDec(b, "yieldPerAcre"));
        if (b.containsKey("remarks"))                r.setRemarks(str(b, "remarks"));
        if (b.containsKey("inspectedBy"))            r.setInspectedBy(str(b, "inspectedBy"));
        // 1st inspection
        if (b.containsKey("plotLocation"))           r.setPlotLocation(str(b, "plotLocation"));
        if (b.containsKey("offTypePercent"))         r.setOffTypePercent(parseDec(b, "offTypePercent"));
        if (b.containsKey("isolationDistance"))      r.setIsolationDistance(parseDec(b, "isolationDistance"));
        if (b.containsKey("cropCondition"))          r.setCropCondition(str(b, "cropCondition"));
        if (b.containsKey("maleSowDate1"))           r.setMaleSowDate1(parseDate(b, "maleSowDate1"));
        if (b.containsKey("maleSowDate2"))           r.setMaleSowDate2(parseDate(b, "maleSowDate2"));
        if (b.containsKey("maleSowDate3"))           r.setMaleSowDate3(parseDate(b, "maleSowDate3"));
        if (b.containsKey("femaleSowDate1"))         r.setFemaleSowDate1(parseDate(b, "femaleSowDate1"));
        if (b.containsKey("femaleSowDate2"))         r.setFemaleSowDate2(parseDate(b, "femaleSowDate2"));
        if (b.containsKey("femaleSowDate3"))         r.setFemaleSowDate3(parseDate(b, "femaleSowDate3"));
        if (b.containsKey("maleAreaAllotted"))       r.setMaleAreaAllotted(parseDec(b, "maleAreaAllotted"));
        if (b.containsKey("femaleAreaAllotted"))     r.setFemaleAreaAllotted(parseDec(b, "femaleAreaAllotted"));
        if (b.containsKey("maleActualAreaSown"))     r.setMaleActualAreaSown(parseDec(b, "maleActualAreaSown"));
        if (b.containsKey("femaleActualAreaSown"))   r.setFemaleActualAreaSown(parseDec(b, "femaleActualAreaSown"));
        if (b.containsKey("maleLotNumber"))          r.setMaleLotNumber(str(b, "maleLotNumber"));
        if (b.containsKey("femaleLotNumber"))        r.setFemaleLotNumber(str(b, "femaleLotNumber"));
        if (b.containsKey("malePlantDistance"))      r.setMalePlantDistance(parseDec(b, "malePlantDistance"));
        if (b.containsKey("femalePlantDistance"))    r.setFemalePlantDistance(parseDec(b, "femalePlantDistance"));
        if (b.containsKey("maleRowDistance"))        r.setMaleRowDistance(parseDec(b, "maleRowDistance"));
        if (b.containsKey("femaleRowDistance"))      r.setFemaleRowDistance(parseDec(b, "femaleRowDistance"));
        if (b.containsKey("maleTotalPopulation"))    r.setMaleTotalPopulation(parseInt(b, "maleTotalPopulation"));
        if (b.containsKey("femaleTotalPopulation"))  r.setFemaleTotalPopulation(parseInt(b, "femaleTotalPopulation"));
        // 2nd / 3rd / 4th shared
        if (b.containsKey("dateOfCrossing"))             r.setDateOfCrossing(parseDate(b, "dateOfCrossing"));
        if (b.containsKey("selfedBollsPercent"))         r.setSelfedBollsPercent(parseDec(b, "selfedBollsPercent"));
        if (b.containsKey("offTypeRemovedCount"))        r.setOffTypeRemovedCount(parseInt(b, "offTypeRemovedCount"));
        if (b.containsKey("emasculation"))               r.setEmasculation(str(b, "emasculation"));
        if (b.containsKey("hybridisation"))              r.setHybridisation(str(b, "hybridisation"));
        if (b.containsKey("meetsFieldStandards"))        r.setMeetsFieldStandards(str(b, "meetsFieldStandards"));
        if (b.containsKey("meetsFieldStandardsComment")) r.setMeetsFieldStandardsComment(str(b, "meetsFieldStandardsComment"));
        // 3rd inspection
        if (b.containsKey("effectiveBollsPerPlant"))     r.setEffectiveBollsPerPlant(parseDec(b, "effectiveBollsPerPlant"));
        if (b.containsKey("removedSelfedBolls"))         r.setRemovedSelfedBolls(str(b, "removedSelfedBolls"));
        if (b.containsKey("crossedBollsPerPlant"))       r.setCrossedBollsPerPlant(parseDec(b, "crossedBollsPerPlant"));
        // 4th inspection
        if (b.containsKey("dateOfCrossingStopped"))      r.setDateOfCrossingStopped(parseDate(b, "dateOfCrossingStopped"));
        if (b.containsKey("selfedBollsPerPlant"))        r.setSelfedBollsPerPlant(parseDec(b, "selfedBollsPerPlant"));
        if (b.containsKey("bollsPerPlant"))              r.setBollsPerPlant(parseDec(b, "bollsPerPlant"));
        if (b.containsKey("alreadyPickedQty"))           r.setAlreadyPickedQty(parseDec(b, "alreadyPickedQty"));
        if (b.containsKey("yieldExpectedQty"))           r.setYieldExpectedQty(parseDec(b, "yieldExpectedQty"));
        if (b.containsKey("yieldExpectingDate"))         r.setYieldExpectingDate(parseDate(b, "yieldExpectingDate"));
        // Captured by the mobile app while the inspector was standing in the field.
        if (b.containsKey("latitude"))                   r.setLatitude(parseDec(b, "latitude"));
        if (b.containsKey("longitude"))                  r.setLongitude(parseDec(b, "longitude"));
        if (b.containsKey("locationAccuracyM"))          r.setLocationAccuracyM(parseDec(b, "locationAccuracyM"));
        if (b.containsKey("capturedAt"))                 r.setCapturedAt(parseDateTime(b, "capturedAt"));
        // Hybrid seed production checks.
        if (b.containsKey("isolationDistanceRequired")) r.setIsolationDistanceRequired(parseDec(b, "isolationDistanceRequired"));
        if (b.containsKey("isolationAdequate"))         r.setIsolationAdequate(parseBool(b, "isolationAdequate"));
        if (b.containsKey("rougingDone"))               r.setRougingDone(parseBool(b, "rougingDone"));
        if (b.containsKey("rougedPlantCount"))          r.setRougedPlantCount(parseInt(b, "rougedPlantCount"));
        if (b.containsKey("rougingRemarks"))            r.setRougingRemarks(str(b, "rougingRemarks"));
        if (b.containsKey("maleRows"))                  r.setMaleRows(parseInt(b, "maleRows"));
        if (b.containsKey("femaleRows"))                r.setFemaleRows(parseInt(b, "femaleRows"));
        if (b.containsKey("detasselingPercent"))        r.setDetasselingPercent(parseDec(b, "detasselingPercent"));
        if (b.containsKey("tasselsRemovedCount"))       r.setTasselsRemovedCount(parseInt(b, "tasselsRemovedCount"));
        if (b.containsKey("sheddingTasselCount"))       r.setSheddingTasselCount(parseInt(b, "sheddingTasselCount"));
        if (b.containsKey("detasselingStatus"))         r.setDetasselingStatus(str(b, "detasselingStatus"));
    }

    private String str(Map<String, Object> b, String key) {
        Object v = b.get(key);
        return v != null ? v.toString() : null;
    }

    private Integer parseInt(Map<String, Object> b, String key) {
        Object v = b.get(key);
        if (v == null || v.toString().isBlank()) return null;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return null; }
    }

    private java.math.BigDecimal parseDec(Map<String, Object> b, String key) {
        Object v = b.get(key);
        if (v == null || v.toString().isBlank()) return null;
        try { return new java.math.BigDecimal(v.toString()); } catch (Exception e) { return null; }
    }

    /** An ISO instant or local date-time from the device clock; null rather than throwing. */
    /** Tri-state on purpose: absent means "not assessed", which is not the same as "no". */
    private Boolean parseBool(java.util.Map<String, Object> b, String key) {
        Object v = b.get(key);
        if (v == null) return null;
        if (v instanceof Boolean bo) return bo;
        String s = v.toString().trim();
        if (s.isEmpty()) return null;
        return "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "1".equals(s);
    }

    private java.time.LocalDateTime parseDateTime(Map<String, Object> b, String key) {
        Object v = b.get(key);
        if (v == null || v.toString().isBlank()) return null;
        String raw = v.toString();
        try { return java.time.LocalDateTime.parse(raw); } catch (Exception ignore) { }
        try { return java.time.OffsetDateTime.parse(raw).toLocalDateTime(); } catch (Exception ignore) { }
        try { return java.time.Instant.parse(raw).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime(); }
        catch (Exception e) { return null; }
    }

    private java.time.LocalDate parseDate(Map<String, Object> b, String key) {
        Object v = b.get(key);
        if (v == null || v.toString().isBlank()) return null;
        try { return java.time.LocalDate.parse(v.toString()); } catch (Exception e) { return null; }
    }

    private FieldInspectionDto toDto(FieldInspection fi) {
        FieldInspectionDto dto = new FieldInspectionDto();
        dto.setId(fi.getId());
        dto.setLotReference(fi.getLotReference());
        dto.setProductionJobId(fi.getProductionJobId());
        dto.setJobReference(fi.getJobReference());
        dto.setItemName(fi.getItemName());
        dto.setItemCode(fi.getItemCode());
        dto.setLocationName(fi.getLocationName());
        dto.setContractReference(fi.getContractReference());
        dto.setInspectorName(fi.getInspectorName());
        dto.setOrganizerName(fi.getOrganizerName());
        dto.setStatus(fi.getStatus());
        dto.setNotes(fi.getNotes());
        dto.setCreatedAt(fi.getCreatedAt());
        dto.setUpdatedAt(fi.getUpdatedAt());
        dto.setRounds(fi.getRounds().stream().map(r -> {
            FieldInspectionDto.RoundDto rd = new FieldInspectionDto.RoundDto();
            rd.setId(r.getId());
            rd.setRoundNumber(r.getRoundNumber());
            rd.setRoundLabel(r.getRoundLabel());
            rd.setInspectionDate(r.getInspectionDate());
            rd.setLotCondition(r.getLotCondition());
            rd.setCropStage(r.getCropStage());
            rd.setFieldAreaAcres(r.getFieldAreaAcres());
            rd.setIsolationZone(r.getIsolationZone());
            rd.setRejectedArea(r.getRejectedArea());
            rd.setOffTypePlantCount(r.getOffTypePlantCount());
            rd.setContaminantPlantCount(r.getContaminantPlantCount());
            rd.setSelfPollinationCount(r.getSelfPollinationCount());
            rd.setRecommendRejection(r.isRecommendRejection());
            rd.setRejectedReasons(r.getRejectedReasons());
            rd.setSowDate1(r.getSowDate1());
            rd.setSowDate2(r.getSowDate2());
            rd.setHarvestDate(r.getHarvestDate());
            rd.setYieldEstimated(r.getYieldEstimated());
            rd.setYieldPerAcre(r.getYieldPerAcre());
            rd.setYield1(r.getYield1());
            rd.setYield2(r.getYield2());
            rd.setRemarks(r.getRemarks());
            rd.setInspectedBy(r.getInspectedBy());
            // 1st inspection
            rd.setPlotLocation(r.getPlotLocation());
            rd.setOffTypePercent(r.getOffTypePercent());
            rd.setIsolationDistance(r.getIsolationDistance());
            rd.setCropCondition(r.getCropCondition());
            rd.setMaleSowDate1(r.getMaleSowDate1());
            rd.setMaleSowDate2(r.getMaleSowDate2());
            rd.setMaleSowDate3(r.getMaleSowDate3());
            rd.setFemaleSowDate1(r.getFemaleSowDate1());
            rd.setFemaleSowDate2(r.getFemaleSowDate2());
            rd.setFemaleSowDate3(r.getFemaleSowDate3());
            rd.setMaleAreaAllotted(r.getMaleAreaAllotted());
            rd.setFemaleAreaAllotted(r.getFemaleAreaAllotted());
            rd.setMaleActualAreaSown(r.getMaleActualAreaSown());
            rd.setFemaleActualAreaSown(r.getFemaleActualAreaSown());
            rd.setMaleLotNumber(r.getMaleLotNumber());
            rd.setFemaleLotNumber(r.getFemaleLotNumber());
            rd.setMalePlantDistance(r.getMalePlantDistance());
            rd.setFemalePlantDistance(r.getFemalePlantDistance());
            rd.setMaleRowDistance(r.getMaleRowDistance());
            rd.setFemaleRowDistance(r.getFemaleRowDistance());
            rd.setMaleTotalPopulation(r.getMaleTotalPopulation());
            rd.setFemaleTotalPopulation(r.getFemaleTotalPopulation());
            // 2nd / 3rd / 4th shared
            rd.setDateOfCrossing(r.getDateOfCrossing());
            rd.setSelfedBollsPercent(r.getSelfedBollsPercent());
            rd.setOffTypeRemovedCount(r.getOffTypeRemovedCount());
            rd.setEmasculation(r.getEmasculation());
            rd.setHybridisation(r.getHybridisation());
            rd.setMeetsFieldStandards(r.getMeetsFieldStandards());
            rd.setMeetsFieldStandardsComment(r.getMeetsFieldStandardsComment());
            // 3rd inspection
            rd.setEffectiveBollsPerPlant(r.getEffectiveBollsPerPlant());
            rd.setRemovedSelfedBolls(r.getRemovedSelfedBolls());
            rd.setCrossedBollsPerPlant(r.getCrossedBollsPerPlant());
            // 4th inspection
            rd.setDateOfCrossingStopped(r.getDateOfCrossingStopped());
            rd.setSelfedBollsPerPlant(r.getSelfedBollsPerPlant());
            rd.setBollsPerPlant(r.getBollsPerPlant());
            rd.setAlreadyPickedQty(r.getAlreadyPickedQty());
            rd.setYieldExpectedQty(r.getYieldExpectedQty());
            rd.setYieldExpectingDate(r.getYieldExpectingDate());
            rd.setLatitude(r.getLatitude());
            rd.setLongitude(r.getLongitude());
            rd.setLocationAccuracyM(r.getLocationAccuracyM());
            rd.setCapturedAt(r.getCapturedAt());
            rd.setIsolationDistanceRequired(r.getIsolationDistanceRequired());
            rd.setIsolationAdequate(r.getIsolationAdequate());
            rd.setRougingDone(r.getRougingDone());
            rd.setRougedPlantCount(r.getRougedPlantCount());
            rd.setRougingRemarks(r.getRougingRemarks());
            rd.setMaleRows(r.getMaleRows());
            rd.setFemaleRows(r.getFemaleRows());
            rd.setDetasselingPercent(r.getDetasselingPercent());
            rd.setTasselsRemovedCount(r.getTasselsRemovedCount());
            rd.setSheddingTasselCount(r.getSheddingTasselCount());
            rd.setDetasselingStatus(r.getDetasselingStatus());
            return rd;
        }).collect(Collectors.toList()));
        return dto;
    }
}

package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateFarmerRequest;
import com.erp.platform.modules.agri.dto.FarmerDto;
import com.erp.platform.modules.agri.dto.FarmerLandRecordDto;
import com.erp.platform.modules.agri.entity.Farmer;
import com.erp.platform.modules.agri.entity.FarmerLandRecord;
import com.erp.platform.modules.agri.entity.Organizer;
import com.erp.platform.modules.agri.repository.FarmerRepository;
import com.erp.platform.modules.agri.repository.FarmerLandRecordRepository;
import com.erp.platform.modules.agri.repository.OrganizerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmerService {

    private final FarmerRepository farmerRepository;
    private final FarmerLandRecordRepository landRecordRepository;
    private final OrganizerRepository organizerRepository;
    private final TenantContext tenantContext;
    private final com.erp.platform.modules.accounting.service.PartyLedgerService partyLedgerService;

    public PageResponse<FarmerDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(farmerRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                .map(f -> toDto(f, loadLandRecords(tenantId, f.getId()))));
    }

    public FarmerDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        Farmer entity = farmerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Farmer not found: " + id));
        return toDto(entity, loadLandRecords(tenantId, id));
    }

    @Transactional
    public FarmerDto create(CreateFarmerRequest request) {
        UUID tenantId = tenantContext.current();
        Farmer entity = new Farmer();
        entity.setTenantId(tenantId);
        applyRequest(entity, request);
        Farmer saved = farmerRepository.save(entity);
        partyLedgerService.ensureLedger(com.erp.platform.modules.accounting.service.PartyLedgerService.PartyType.GROWER, saved.getName(), saved.getId(), saved.getFarmerCode());
        syncOrganizer(saved);
        List<FarmerLandRecord> records = saveLandRecords(tenantId, saved.getId(), request.getLandRecords());
        return toDto(saved, records.stream().map(this::toLandDto).collect(Collectors.toList()));
    }

    @Transactional
    public FarmerDto update(UUID id, CreateFarmerRequest request) {
        UUID tenantId = tenantContext.current();
        Farmer entity = farmerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Farmer not found: " + id));
        applyRequest(entity, request);
        Farmer saved = farmerRepository.save(entity);
        partyLedgerService.ensureLedger(com.erp.platform.modules.accounting.service.PartyLedgerService.PartyType.GROWER, saved.getName(), saved.getId(), saved.getFarmerCode());
        syncOrganizer(saved);
        List<FarmerLandRecord> records = saveLandRecords(tenantId, saved.getId(), request.getLandRecords());
        return toDto(saved, records.stream().map(this::toLandDto).collect(Collectors.toList()));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        Farmer entity = farmerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Farmer not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        farmerRepository.save(entity);
    }

    private void syncOrganizer(Farmer f) {
        if (f.isOrganizer()) {
            if (f.getOrganizerId() == null) {
                Organizer o = new Organizer();
                o.setTenantId(f.getTenantId());
                applyFarmerToOrganizer(o, f);
                Organizer saved = organizerRepository.save(o);
                f.setOrganizerId(saved.getId());
                farmerRepository.save(f);
            } else {
                organizerRepository.findByTenantIdAndIdAndDeletedAtIsNull(f.getTenantId(), f.getOrganizerId())
                        .ifPresent(o -> { applyFarmerToOrganizer(o, f); organizerRepository.save(o); });
            }
        } else {
            if (f.getOrganizerId() != null) {
                organizerRepository.findByTenantIdAndIdAndDeletedAtIsNull(f.getTenantId(), f.getOrganizerId())
                        .ifPresent(o -> { o.setActive(false); organizerRepository.save(o); });
            }
        }
    }

    private void applyFarmerToOrganizer(Organizer o, Farmer f) {
        o.setName(f.getName());
        o.setCode(f.getFarmerCode());
        o.setMobile(f.getMobileNo());
        o.setOfficePhone(f.getPhoneNumber());
        o.setBankAccountNo(f.getBankAccountNo());
        o.setBankIfsc(f.getBankIfsc());
        o.setBankBranch(f.getBankBranch());
        o.setBankName(f.getBankName());
        o.setStateId(f.getStateId());
        o.setStateName(f.getStateName());
        o.setDistrictId(f.getDistrictId());
        o.setDistrictName(f.getDistrictName());
        o.setVillageId(f.getVillageId());
        o.setVillageName(f.getVillageName());
        o.setZip(f.getZip());
        o.setActive(f.isActive());
        o.setAdharNo(f.getAdharCard());
    }

    private List<FarmerLandRecord> saveLandRecords(UUID tenantId, UUID farmerId, List<FarmerLandRecordDto> dtos) {
        landRecordRepository.deleteByTenantIdAndFarmerId(tenantId, farmerId);
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        List<FarmerLandRecord> records = dtos.stream().map(dto -> {
            FarmerLandRecord r = new FarmerLandRecord();
            r.setTenantId(tenantId);
            r.setFarmerId(farmerId);
            r.setVillageId(dto.getVillageId());
            r.setVillageName(dto.getVillageName());
            r.setPlotSurveyNo(dto.getPlotSurveyNo());
            r.setAcreage(dto.getAcreage());
            r.setLandType(dto.getLandType());
            return r;
        }).collect(Collectors.toList());
        return landRecordRepository.saveAll(records);
    }

    private List<FarmerLandRecordDto> loadLandRecords(UUID tenantId, UUID farmerId) {
        return landRecordRepository.findByTenantIdAndFarmerIdAndDeletedAtIsNull(tenantId, farmerId)
                .stream().map(this::toLandDto).collect(Collectors.toList());
    }

    private void applyRequest(Farmer e, CreateFarmerRequest r) {
        e.setName(r.getName());
        e.setPhoto(r.getPhoto());
        e.setFathersName(r.getFathersName());
        e.setNearestRlyStn(r.getNearestRlyStn());
        e.setTelegraphOff(r.getTelegraphOff());
        e.setStateId(r.getStateId());
        e.setStateName(r.getStateName());
        e.setDistrictId(r.getDistrictId());
        e.setDistrictName(r.getDistrictName());
        e.setZip(r.getZip());
        e.setFarmerCode(r.getFarmerCode());
        e.setShareholder(r.isShareholder());
        e.setFolioNo(r.getFolioNo());
        e.setMobileNo(r.getMobileNo());
        e.setFarmerType(r.getFarmerType());
        e.setAccountHeads(r.getAccountHeads());
        e.setAdharCard(r.getAdharCard());
        e.setBankName(r.getBankName());
        e.setBankAccountNo(r.getBankAccountNo());
        e.setBankIfsc(r.getBankIfsc());
        e.setBankBranch(r.getBankBranch());
        e.setDocumentType(r.getDocumentType());
        e.setVillageId(r.getVillageId());
        e.setVillageName(r.getVillageName());
        e.setDistanceFromHighway(r.getDistanceFromHighway());
        e.setDistanceFromHighwayUnit(r.getDistanceFromHighwayUnit());
        e.setNearestTown(r.getNearestTown());
        e.setMandalId(r.getMandalId());
        e.setMandalName(r.getMandalName());
        e.setActive(r.isActive());
        e.setOrganizer(r.isOrganizer());
        e.setPhoneNumber(r.getPhoneNumber());
        e.setDistanceFromPlant(r.getDistanceFromPlant());
        e.setDistanceFromPlantUnit(r.getDistanceFromPlantUnit());
    }

    private FarmerDto toDto(Farmer e, List<FarmerLandRecordDto> landRecords) {
        FarmerDto dto = new FarmerDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setPhoto(e.getPhoto());
        dto.setFathersName(e.getFathersName());
        dto.setNearestRlyStn(e.getNearestRlyStn());
        dto.setTelegraphOff(e.getTelegraphOff());
        dto.setStateId(e.getStateId());
        dto.setStateName(e.getStateName());
        dto.setDistrictId(e.getDistrictId());
        dto.setDistrictName(e.getDistrictName());
        dto.setZip(e.getZip());
        dto.setFarmerCode(e.getFarmerCode());
        dto.setShareholder(e.isShareholder());
        dto.setFolioNo(e.getFolioNo());
        dto.setMobileNo(e.getMobileNo());
        dto.setFarmerType(e.getFarmerType());
        dto.setAccountHeads(e.getAccountHeads());
        dto.setAdharCard(e.getAdharCard());
        dto.setBankName(e.getBankName());
        dto.setBankAccountNo(e.getBankAccountNo());
        dto.setBankIfsc(e.getBankIfsc());
        dto.setBankBranch(e.getBankBranch());
        dto.setDocumentType(e.getDocumentType());
        dto.setVillageId(e.getVillageId());
        dto.setVillageName(e.getVillageName());
        dto.setDistanceFromHighway(e.getDistanceFromHighway());
        dto.setDistanceFromHighwayUnit(e.getDistanceFromHighwayUnit());
        dto.setNearestTown(e.getNearestTown());
        dto.setMandalId(e.getMandalId());
        dto.setMandalName(e.getMandalName());
        dto.setOrganizerId(e.getOrganizerId());
        dto.setActive(e.isActive());
        dto.setOrganizer(e.isOrganizer());
        dto.setPhoneNumber(e.getPhoneNumber());
        dto.setDistanceFromPlant(e.getDistanceFromPlant());
        dto.setDistanceFromPlantUnit(e.getDistanceFromPlantUnit());
        dto.setLandRecords(landRecords);
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }

    private FarmerLandRecordDto toLandDto(FarmerLandRecord r) {
        FarmerLandRecordDto dto = new FarmerLandRecordDto();
        dto.setId(r.getId());
        dto.setVillageId(r.getVillageId());
        dto.setVillageName(r.getVillageName());
        dto.setPlotSurveyNo(r.getPlotSurveyNo());
        dto.setAcreage(r.getAcreage());
        dto.setLandType(r.getLandType());
        return dto;
    }
}

package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateOrganizerRequest;
import com.erp.platform.modules.agri.dto.OrganizerDto;
import com.erp.platform.modules.agri.entity.Organizer;
import com.erp.platform.modules.agri.repository.OrganizerRepository;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.master.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizerService {

    private final OrganizerRepository organizerRepository;
    private final VendorRepository vendorRepository;
    private final TenantContext tenantContext;
    private final com.erp.platform.modules.accounting.service.PartyLedgerService partyLedgerService;

    public PageResponse<OrganizerDto> list(Pageable pageable) {
        return PageResponse.of(organizerRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public OrganizerDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        Organizer entity = organizerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Organizer not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public OrganizerDto create(CreateOrganizerRequest request) {
        Organizer entity = new Organizer();
        entity.setTenantId(tenantContext.current());
        applyRequest(entity, request);
        syncVendor(entity);
        Organizer saved = organizerRepository.save(entity);
        partyLedgerService.ensureLedger(com.erp.platform.modules.accounting.service.PartyLedgerService.PartyType.ORGANIZER, saved.getName(), saved.getId(), saved.getCode());
        return toDto(saved);
    }

    @Transactional
    public OrganizerDto update(UUID id, CreateOrganizerRequest request) {
        UUID tenantId = tenantContext.current();
        Organizer entity = organizerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Organizer not found: " + id));
        applyRequest(entity, request);
        syncVendor(entity);
        Organizer saved = organizerRepository.save(entity);
        partyLedgerService.ensureLedger(com.erp.platform.modules.accounting.service.PartyLedgerService.PartyType.ORGANIZER, saved.getName(), saved.getId(), saved.getCode());
        return toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        Organizer entity = organizerRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("Organizer not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        organizerRepository.save(entity);
    }

    private void applyRequest(Organizer e, CreateOrganizerRequest r) {
        e.setName(r.getName());
        e.setPhoto(r.getPhoto());
        e.setCode(r.getCode());
        e.setOfficeAddress(r.getOfficeAddress());
        e.setPlantAddress(r.getPlantAddress());
        e.setVillageId(r.getVillageId());
        e.setVillageName(r.getVillageName());
        e.setStateId(r.getStateId());
        e.setStateName(r.getStateName());
        e.setDistrictId(r.getDistrictId());
        e.setDistrictName(r.getDistrictName());
        e.setZip(r.getZip());
        e.setOfficePhone(r.getOfficePhone());
        e.setPlantPhone(r.getPlantPhone());
        e.setContactPerson(r.getContactPerson());
        e.setRelationshipWithCompany(r.getRelationshipWithCompany());
        e.setMobile(r.getMobile());
        e.setEmail(r.getEmail());
        e.setPanNo(r.getPanNo());
        e.setTinNo(r.getTinNo());
        e.setVatNo(r.getVatNo());
        e.setSstNo(r.getSstNo());
        e.setAdharNo(r.getAdharNo());
        e.setOwnGodownSqft(r.getOwnGodownSqft());
        e.setHiredGodownSqft(r.getHiredGodownSqft());
        e.setOwnMachineTph(r.getOwnMachineTph());
        e.setHiredMachineTph(r.getHiredMachineTph());
        e.setRegCertNo(r.getRegCertNo());
        e.setSeedLicenseNo(r.getSeedLicenseNo());
        e.setSeedLicenseValidity(r.getSeedLicenseValidity());
        e.setSeedCertAgency(r.getSeedCertAgency());
        e.setPlantRegNo(r.getPlantRegNo());
        e.setBankAccountNo(r.getBankAccountNo());
        e.setBankBranch(r.getBankBranch());
        e.setBankName(r.getBankName());
        e.setBankIfsc(r.getBankIfsc());
        e.setActive(r.isActive());
        e.setSupplier(r.isSupplier());
        e.setProductionAreaIds(r.getProductionAreaIds());
        e.setProductionAreaNames(r.getProductionAreaNames());
        e.setExperienceCrops(r.getExperienceCrops());
        e.setAccountHeads(r.getAccountHeads());
    }

    private void syncVendor(Organizer e) {
        if (e.isSupplier()) {
            if (e.getVendorId() == null) {
                Vendor v = new Vendor();
                v.setTenantId(e.getTenantId());
                applyOrganizerToVendor(v, e);
                Vendor saved = vendorRepository.save(v);
                e.setVendorId(saved.getId());
            } else {
                vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(e.getTenantId(), e.getVendorId())
                        .ifPresent(v -> {
                            applyOrganizerToVendor(v, e);
                            vendorRepository.save(v);
                        });
            }
        } else {
            if (e.getVendorId() != null) {
                vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(e.getTenantId(), e.getVendorId())
                        .ifPresent(v -> {
                            v.setActive(false);
                            vendorRepository.save(v);
                        });
            }
        }
    }

    private void applyOrganizerToVendor(Vendor v, Organizer o) {
        v.setName(o.getName());
        v.setCode(o.getCode());
        v.setEmail(o.getEmail());
        v.setPhone(o.getOfficePhone());
        v.setMobile(o.getMobile());
        v.setContactPerson(o.getContactPerson());
        v.setAddress(o.getOfficeAddress());
        v.setState(o.getStateName());
        v.setPostalCode(o.getZip());
        v.setPanNumber(o.getPanNo());
        v.setBankName(o.getBankName());
        v.setBankAccount(o.getBankAccountNo());
        v.setBankRoutingCode(o.getBankIfsc());
        v.setActive(o.isActive());
        v.setCategory(Vendor.VendorCategory.SUPPLIER);
    }

    private OrganizerDto toDto(Organizer e) {
        OrganizerDto dto = new OrganizerDto();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setPhoto(e.getPhoto());
        dto.setCode(e.getCode());
        dto.setOfficeAddress(e.getOfficeAddress());
        dto.setPlantAddress(e.getPlantAddress());
        dto.setVillageId(e.getVillageId());
        dto.setVillageName(e.getVillageName());
        dto.setStateId(e.getStateId());
        dto.setStateName(e.getStateName());
        dto.setDistrictId(e.getDistrictId());
        dto.setDistrictName(e.getDistrictName());
        dto.setZip(e.getZip());
        dto.setOfficePhone(e.getOfficePhone());
        dto.setPlantPhone(e.getPlantPhone());
        dto.setContactPerson(e.getContactPerson());
        dto.setRelationshipWithCompany(e.getRelationshipWithCompany());
        dto.setMobile(e.getMobile());
        dto.setEmail(e.getEmail());
        dto.setPanNo(e.getPanNo());
        dto.setTinNo(e.getTinNo());
        dto.setVatNo(e.getVatNo());
        dto.setSstNo(e.getSstNo());
        dto.setAdharNo(e.getAdharNo());
        dto.setOwnGodownSqft(e.getOwnGodownSqft());
        dto.setHiredGodownSqft(e.getHiredGodownSqft());
        dto.setOwnMachineTph(e.getOwnMachineTph());
        dto.setHiredMachineTph(e.getHiredMachineTph());
        dto.setRegCertNo(e.getRegCertNo());
        dto.setSeedLicenseNo(e.getSeedLicenseNo());
        dto.setSeedLicenseValidity(e.getSeedLicenseValidity());
        dto.setSeedCertAgency(e.getSeedCertAgency());
        dto.setPlantRegNo(e.getPlantRegNo());
        dto.setBankAccountNo(e.getBankAccountNo());
        dto.setBankBranch(e.getBankBranch());
        dto.setBankName(e.getBankName());
        dto.setBankIfsc(e.getBankIfsc());
        dto.setActive(e.isActive());
        dto.setSupplier(e.isSupplier());
        dto.setProductionAreaIds(e.getProductionAreaIds());
        dto.setProductionAreaNames(e.getProductionAreaNames());
        dto.setExperienceCrops(e.getExperienceCrops());
        dto.setAccountHeads(e.getAccountHeads());
        dto.setVendorId(e.getVendorId());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}

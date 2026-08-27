package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.entity.GeoCity;
import com.erp.platform.modules.master.entity.GeoCountry;
import com.erp.platform.modules.master.entity.GeoDistrict;
import com.erp.platform.modules.master.entity.GeoMandal;
import com.erp.platform.modules.master.entity.GeoState;
import com.erp.platform.modules.master.repository.GeoCityRepository;
import com.erp.platform.modules.master.repository.GeoCountryRepository;
import com.erp.platform.modules.master.repository.GeoDistrictRepository;
import com.erp.platform.modules.master.repository.GeoMandalRepository;
import com.erp.platform.modules.master.repository.GeoStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeographicService {

    private final GeoCountryRepository  countryRepo;
    private final GeoStateRepository    stateRepo;
    private final GeoCityRepository     cityRepo;
    private final GeoDistrictRepository districtRepo;
    private final GeoMandalRepository   mandalRepo;
    private final TenantContext         tenantContext;

    // ── Countries ────────────────────────────────────────────────────────────

    public PageResponse<GeoCountry> listCountries(Pageable pageable) {
        UUID t = tenantContext.current();
        return PageResponse.of(countryRepo.findByTenantIdAndDeletedAtIsNull(t, pageable));
    }

    public List<GeoCountry> allCountries() {
        return countryRepo.findByTenantIdAndDeletedAtIsNullOrderByNameAsc(tenantContext.current());
    }

    public GeoCountry getCountry(UUID id) {
        return countryRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Country not found: " + id));
    }

    @Transactional
    public GeoCountry createCountry(GeoCountry req) {
        req.setTenantId(tenantContext.current());
        req.setActive(req.isActive());
        return countryRepo.save(req);
    }

    @Transactional
    public GeoCountry updateCountry(UUID id, GeoCountry req) {
        GeoCountry c = getCountry(id);
        c.setName(req.getName());
        if (req.getIsoCode2()       != null) c.setIsoCode2(req.getIsoCode2());
        if (req.getIsoCode3()       != null) c.setIsoCode3(req.getIsoCode3());
        if (req.getDialingCode()    != null) c.setDialingCode(req.getDialingCode());
        if (req.getCurrencyCode()   != null) c.setCurrencyCode(req.getCurrencyCode());
        if (req.getCurrencySymbol() != null) c.setCurrencySymbol(req.getCurrencySymbol());
        if (req.getNotes()          != null) c.setNotes(req.getNotes());
        c.setActive(req.isActive());
        return countryRepo.save(c);
    }

    @Transactional
    public void deleteCountry(UUID id) {
        GeoCountry c = getCountry(id);
        c.setDeletedAt(LocalDateTime.now());
        countryRepo.save(c);
    }

    // ── States ───────────────────────────────────────────────────────────────

    public PageResponse<GeoState> listStates(UUID countryId, Pageable pageable) {
        UUID t = tenantContext.current();
        return PageResponse.of(stateRepo.findByTenantIdAndDeletedAtIsNull(t, pageable));
    }

    public List<GeoState> statesByCountry(UUID countryId) {
        return stateRepo.findByTenantIdAndCountryIdAndDeletedAtIsNullOrderByNameAsc(tenantContext.current(), countryId);
    }

    public GeoState getState(UUID id) {
        return stateRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("State not found: " + id));
    }

    @Transactional
    public GeoState createState(GeoState req) {
        UUID t = tenantContext.current();
        req.setTenantId(t);
        if (req.getCountryId() != null) {
            countryRepo.findByTenantIdAndIdAndDeletedAtIsNull(t, req.getCountryId())
                    .ifPresent(c -> req.setCountryName(c.getName()));
        }
        return stateRepo.save(req);
    }

    @Transactional
    public GeoState updateState(UUID id, GeoState req) {
        GeoState s = getState(id);
        s.setName(req.getName());
        if (req.getCode()      != null) s.setCode(req.getCode());
        if (req.getCountryId() != null) {
            s.setCountryId(req.getCountryId());
            countryRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), req.getCountryId())
                    .ifPresent(c -> s.setCountryName(c.getName()));
        }
        s.setActive(req.isActive());
        return stateRepo.save(s);
    }

    @Transactional
    public void deleteState(UUID id) {
        GeoState s = getState(id);
        s.setDeletedAt(LocalDateTime.now());
        stateRepo.save(s);
    }

    // ── Cities ───────────────────────────────────────────────────────────────

    public PageResponse<GeoCity> listCities(UUID stateId, UUID districtId, Pageable pageable) {
        UUID t = tenantContext.current();
        if (districtId != null) return PageResponse.of(cityRepo.findByTenantIdAndDistrictIdAndDeletedAtIsNull(t, districtId, pageable));
        return PageResponse.of(cityRepo.findByTenantIdAndDeletedAtIsNull(t, pageable));
    }

    public List<GeoCity> citiesByState(UUID stateId) {
        return cityRepo.findByTenantIdAndStateIdAndDeletedAtIsNullOrderByNameAsc(tenantContext.current(), stateId);
    }

    public List<GeoCity> citiesByDistrict(UUID districtId) {
        return cityRepo.findByTenantIdAndDistrictIdAndDeletedAtIsNullOrderByNameAsc(tenantContext.current(), districtId);
    }

    public GeoCity getCity(UUID id) {
        return cityRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("City not found: " + id));
    }

    @Transactional
    public GeoCity createCity(GeoCity req) {
        UUID t = tenantContext.current();
        req.setTenantId(t);
        if (req.getStateId() != null) {
            stateRepo.findByTenantIdAndIdAndDeletedAtIsNull(t, req.getStateId()).ifPresent(s -> {
                req.setStateName(s.getName());
                req.setCountryId(s.getCountryId());
                req.setCountryName(s.getCountryName());
            });
        }
        if (req.getDistrictId() != null) {
            districtRepo.findByTenantIdAndIdAndDeletedAtIsNull(t, req.getDistrictId()).ifPresent(d -> {
                req.setDistrictName(d.getName());
                if (req.getStateId() == null) { req.setStateId(d.getStateId()); req.setStateName(d.getStateName()); }
            });
        }
        return cityRepo.save(req);
    }

    @Transactional
    public GeoCity updateCity(UUID id, GeoCity req) {
        GeoCity c = getCity(id);
        c.setName(req.getName());
        if (req.getCode()    != null) c.setCode(req.getCode());
        if (req.getStateId() != null) {
            c.setStateId(req.getStateId());
            stateRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), req.getStateId()).ifPresent(s -> {
                c.setStateName(s.getName());
                c.setCountryId(s.getCountryId());
                c.setCountryName(s.getCountryName());
            });
        }
        if (req.getDistrictId() != null) {
            c.setDistrictId(req.getDistrictId());
            districtRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), req.getDistrictId())
                    .ifPresent(d -> c.setDistrictName(d.getName()));
        }
        c.setActive(req.isActive());
        return cityRepo.save(c);
    }

    @Transactional
    public void deleteCity(UUID id) {
        GeoCity c = getCity(id);
        c.setDeletedAt(LocalDateTime.now());
        cityRepo.save(c);
    }

    // ── Districts ────────────────────────────────────────────────────────────

    public PageResponse<GeoDistrict> listDistricts(UUID stateId, Pageable pageable) {
        UUID t = tenantContext.current();
        return PageResponse.of(stateId != null
                ? districtRepo.findByTenantIdAndStateIdAndDeletedAtIsNull(t, stateId, pageable)
                : districtRepo.findByTenantIdAndDeletedAtIsNull(t, pageable));
    }

    public List<GeoDistrict> districtsByState(UUID stateId) {
        return districtRepo.findByTenantIdAndStateIdAndDeletedAtIsNullOrderByNameAsc(tenantContext.current(), stateId);
    }

    public GeoDistrict getDistrict(UUID id) {
        return districtRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("District not found: " + id));
    }

    @Transactional
    public GeoDistrict createDistrict(GeoDistrict req) {
        UUID t = tenantContext.current();
        req.setTenantId(t);
        if (req.getStateId() != null) {
            stateRepo.findByTenantIdAndIdAndDeletedAtIsNull(t, req.getStateId()).ifPresent(s -> {
                req.setStateName(s.getName());
                req.setCountryId(s.getCountryId());
                req.setCountryName(s.getCountryName());
            });
        }
        return districtRepo.save(req);
    }

    @Transactional
    public GeoDistrict updateDistrict(UUID id, GeoDistrict req) {
        GeoDistrict d = getDistrict(id);
        d.setName(req.getName());
        if (req.getCode()    != null) d.setCode(req.getCode());
        if (req.getStateId() != null) {
            d.setStateId(req.getStateId());
            stateRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), req.getStateId()).ifPresent(s -> {
                d.setStateName(s.getName());
                d.setCountryId(s.getCountryId());
                d.setCountryName(s.getCountryName());
            });
        }
        d.setActive(req.isActive());
        return districtRepo.save(d);
    }

    @Transactional
    public void deleteDistrict(UUID id) {
        GeoDistrict d = getDistrict(id);
        d.setDeletedAt(LocalDateTime.now());
        districtRepo.save(d);
    }

    // ── Mandals ───────────────────────────────────────────────────────────────

    public PageResponse<GeoMandal> listMandals(UUID districtId, Pageable pageable) {
        UUID t = tenantContext.current();
        return PageResponse.of(districtId != null
                ? mandalRepo.findByTenantIdAndDistrictIdAndDeletedAtIsNull(t, districtId, pageable)
                : mandalRepo.findByTenantIdAndDeletedAtIsNull(t, pageable));
    }

    public List<GeoMandal> mandalsByDistrict(UUID districtId) {
        return mandalRepo.findByTenantIdAndDistrictIdAndDeletedAtIsNullOrderByNameAsc(tenantContext.current(), districtId);
    }

    public GeoMandal getMandal(UUID id) {
        return mandalRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Mandal not found: " + id));
    }

    @Transactional
    public GeoMandal createMandal(GeoMandal req) {
        UUID t = tenantContext.current();
        req.setTenantId(t);
        if (req.getDistrictId() != null) {
            districtRepo.findByTenantIdAndIdAndDeletedAtIsNull(t, req.getDistrictId()).ifPresent(dist -> {
                req.setDistrictName(dist.getName());
                req.setStateId(dist.getStateId());
                req.setStateName(dist.getStateName());
            });
        }
        return mandalRepo.save(req);
    }

    @Transactional
    public GeoMandal updateMandal(UUID id, GeoMandal req) {
        GeoMandal m = getMandal(id);
        m.setName(req.getName());
        if (req.getCode()       != null) m.setCode(req.getCode());
        if (req.getDistrictId() != null) {
            m.setDistrictId(req.getDistrictId());
            districtRepo.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), req.getDistrictId()).ifPresent(dist -> {
                m.setDistrictName(dist.getName());
                m.setStateId(dist.getStateId());
                m.setStateName(dist.getStateName());
            });
        }
        m.setActive(req.isActive());
        return mandalRepo.save(m);
    }

    @Transactional
    public void deleteMandal(UUID id) {
        GeoMandal m = getMandal(id);
        m.setDeletedAt(LocalDateTime.now());
        mandalRepo.save(m);
    }

    // ── Seed India ────────────────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> seedIndia() {
        UUID t = tenantContext.current();

        GeoCountry india = countryRepo.findByTenantIdAndIsoCode2AndDeletedAtIsNull(t, "IN")
                .orElseGet(() -> {
                    GeoCountry c = new GeoCountry();
                    c.setTenantId(t);
                    c.setName("India");
                    c.setIsoCode2("IN");
                    c.setIsoCode3("IND");
                    c.setDialingCode("+91");
                    c.setCurrencyCode("INR");
                    c.setCurrencySymbol("₹");
                    c.setActive(true);
                    return countryRepo.save(c);
                });

        String[] stateNames = {
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
            "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
            "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
            "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
            "Andaman and Nicobar Islands", "Chandigarh",
            "Dadra and Nagar Haveli and Daman and Diu",
            "Delhi", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
        };

        int created = 0;
        UUID countryId = india.getId();
        for (String sn : stateNames) {
            if (stateRepo.findByTenantIdAndNameAndCountryIdAndDeletedAtIsNull(t, sn, countryId).isEmpty()) {
                GeoState s = new GeoState();
                s.setTenantId(t);
                s.setName(sn);
                s.setCountryId(countryId);
                s.setCountryName("India");
                s.setActive(true);
                stateRepo.save(s);
                created++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("country", india.getName());
        result.put("statesCreated", created);
        result.put("totalStates", stateNames.length);
        return result;
    }
}

package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.master.entity.GeoCity;
import com.erp.platform.modules.master.entity.GeoCountry;
import com.erp.platform.modules.master.entity.GeoDistrict;
import com.erp.platform.modules.master.entity.GeoMandal;
import com.erp.platform.modules.master.entity.GeoState;
import com.erp.platform.modules.master.service.GeographicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/master/geographic")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class GeographicController {

    private final GeographicService geoService;

    // ── Countries ──────────────────────────────────────────────────────────

    @GetMapping("/countries")
    public ResponseEntity<ApiResponse<PageResponse<GeoCountry>>> listCountries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                geoService.listCountries(PageRequest.of(page, size, Sort.by("name")))));
    }

    @GetMapping("/countries/all")
    public ResponseEntity<ApiResponse<List<GeoCountry>>> allCountries() {
        return ResponseEntity.ok(ApiResponse.success(geoService.allCountries()));
    }

    @GetMapping("/countries/{id}")
    public ResponseEntity<ApiResponse<GeoCountry>> getCountry(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(geoService.getCountry(id)));
    }

    @PostMapping("/countries")
    public ResponseEntity<ApiResponse<GeoCountry>> createCountry(@RequestBody GeoCountry req) {
        return ResponseEntity.ok(ApiResponse.success(geoService.createCountry(req), "Country created"));
    }

    @PutMapping("/countries/{id}")
    public ResponseEntity<ApiResponse<GeoCountry>> updateCountry(@PathVariable UUID id, @RequestBody GeoCountry req) {
        return ResponseEntity.ok(ApiResponse.success(geoService.updateCountry(id, req)));
    }

    @DeleteMapping("/countries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCountry(@PathVariable UUID id) {
        geoService.deleteCountry(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── States ─────────────────────────────────────────────────────────────

    @GetMapping("/states")
    public ResponseEntity<ApiResponse<PageResponse<GeoState>>> listStates(
            @RequestParam(required = false) UUID countryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                geoService.listStates(countryId, PageRequest.of(page, size, Sort.by("name")))));
    }

    @GetMapping("/states/by-country/{countryId}")
    public ResponseEntity<ApiResponse<List<GeoState>>> statesByCountry(@PathVariable UUID countryId) {
        return ResponseEntity.ok(ApiResponse.success(geoService.statesByCountry(countryId)));
    }

    @GetMapping("/states/{id}")
    public ResponseEntity<ApiResponse<GeoState>> getState(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(geoService.getState(id)));
    }

    @PostMapping("/states")
    public ResponseEntity<ApiResponse<GeoState>> createState(@RequestBody GeoState req) {
        return ResponseEntity.ok(ApiResponse.success(geoService.createState(req), "State created"));
    }

    @PutMapping("/states/{id}")
    public ResponseEntity<ApiResponse<GeoState>> updateState(@PathVariable UUID id, @RequestBody GeoState req) {
        return ResponseEntity.ok(ApiResponse.success(geoService.updateState(id, req)));
    }

    @DeleteMapping("/states/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteState(@PathVariable UUID id) {
        geoService.deleteState(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Cities ─────────────────────────────────────────────────────────────

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<PageResponse<GeoCity>>> listCities(
            @RequestParam(required = false) UUID stateId,
            @RequestParam(required = false) UUID districtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                geoService.listCities(stateId, districtId, PageRequest.of(page, size, Sort.by("name")))));
    }

    @GetMapping("/cities/by-state/{stateId}")
    public ResponseEntity<ApiResponse<List<GeoCity>>> citiesByState(@PathVariable UUID stateId) {
        return ResponseEntity.ok(ApiResponse.success(geoService.citiesByState(stateId)));
    }

    @GetMapping("/cities/by-district/{districtId}")
    public ResponseEntity<ApiResponse<List<GeoCity>>> citiesByDistrict(@PathVariable UUID districtId) {
        return ResponseEntity.ok(ApiResponse.success(geoService.citiesByDistrict(districtId)));
    }

    @GetMapping("/cities/{id}")
    public ResponseEntity<ApiResponse<GeoCity>> getCity(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(geoService.getCity(id)));
    }

    @PostMapping("/cities")
    public ResponseEntity<ApiResponse<GeoCity>> createCity(@RequestBody GeoCity req) {
        return ResponseEntity.ok(ApiResponse.success(geoService.createCity(req), "City created"));
    }

    @PutMapping("/cities/{id}")
    public ResponseEntity<ApiResponse<GeoCity>> updateCity(@PathVariable UUID id, @RequestBody GeoCity req) {
        return ResponseEntity.ok(ApiResponse.success(geoService.updateCity(id, req)));
    }

    @DeleteMapping("/cities/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCity(@PathVariable UUID id) {
        geoService.deleteCity(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Districts ──────────────────────────────────────────────────────────

    @GetMapping("/districts")
    public ResponseEntity<ApiResponse<PageResponse<GeoDistrict>>> listDistricts(
            @RequestParam(required = false) UUID stateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                geoService.listDistricts(stateId, PageRequest.of(page, size, Sort.by("name")))));
    }

    @GetMapping("/districts/by-state/{stateId}")
    public ResponseEntity<ApiResponse<List<GeoDistrict>>> districtsByState(@PathVariable UUID stateId) {
        return ResponseEntity.ok(ApiResponse.success(geoService.districtsByState(stateId)));
    }

    @GetMapping("/districts/{id}")
    public ResponseEntity<ApiResponse<GeoDistrict>> getDistrict(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(geoService.getDistrict(id)));
    }

    @PostMapping("/districts")
    public ResponseEntity<ApiResponse<GeoDistrict>> createDistrict(@RequestBody GeoDistrict req) {
        return ResponseEntity.ok(ApiResponse.success(geoService.createDistrict(req), "District created"));
    }

    @PutMapping("/districts/{id}")
    public ResponseEntity<ApiResponse<GeoDistrict>> updateDistrict(@PathVariable UUID id, @RequestBody GeoDistrict req) {
        return ResponseEntity.ok(ApiResponse.success(geoService.updateDistrict(id, req)));
    }

    @DeleteMapping("/districts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDistrict(@PathVariable UUID id) {
        geoService.deleteDistrict(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Mandals ────────────────────────────────────────────────────────────

    @GetMapping("/mandals")
    public ResponseEntity<ApiResponse<PageResponse<GeoMandal>>> listMandals(
            @RequestParam(required = false) UUID districtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                geoService.listMandals(districtId, PageRequest.of(page, size, Sort.by("name")))));
    }

    @GetMapping("/mandals/by-district/{districtId}")
    public ResponseEntity<ApiResponse<List<GeoMandal>>> mandalsByDistrict(@PathVariable UUID districtId) {
        return ResponseEntity.ok(ApiResponse.success(geoService.mandalsByDistrict(districtId)));
    }

    @GetMapping("/mandals/{id}")
    public ResponseEntity<ApiResponse<GeoMandal>> getMandal(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(geoService.getMandal(id)));
    }

    @PostMapping("/mandals")
    public ResponseEntity<ApiResponse<GeoMandal>> createMandal(@RequestBody GeoMandal req) {
        return ResponseEntity.ok(ApiResponse.success(geoService.createMandal(req), "Mandal created"));
    }

    @PutMapping("/mandals/{id}")
    public ResponseEntity<ApiResponse<GeoMandal>> updateMandal(@PathVariable UUID id, @RequestBody GeoMandal req) {
        return ResponseEntity.ok(ApiResponse.success(geoService.updateMandal(id, req)));
    }

    @DeleteMapping("/mandals/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMandal(@PathVariable UUID id) {
        geoService.deleteMandal(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Seed ───────────────────────────────────────────────────────────────

    @PostMapping("/seed-india")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedIndia() {
        return ResponseEntity.ok(ApiResponse.success(geoService.seedIndia(), "India data seeded"));
    }
}

package com.erp.platform.modules.organization.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.organization.dto.CompanyDto;
import com.erp.platform.modules.organization.dto.CreateCompanyRequest;
import com.erp.platform.modules.organization.entity.Company;
import com.erp.platform.modules.organization.repository.CompanyRepository;
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
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final TenantContext tenantContext;

    public PageResponse<CompanyDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        return PageResponse.of(companyRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable)
                .map(this::toDto));
    }

    public CompanyDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public CompanyDto create(CreateCompanyRequest request) {
        UUID tenantId = tenantContext.current();
        Company company = new Company();
        company.setTenantId(tenantId);
        applyRequest(company, request);
        company = companyRepository.save(company);
        log.info("Company created: id={}, name={}", company.getId(), company.getName());
        return toDto(company);
    }

    @Transactional
    public CompanyDto update(UUID id, CreateCompanyRequest request) {
        Company company = findOrThrow(id);
        applyRequest(company, request);
        return toDto(companyRepository.save(company));
    }

    @Transactional
    public void delete(UUID id) {
        Company company = findOrThrow(id);
        company.setDeletedAt(LocalDateTime.now());
        companyRepository.save(company);
        log.info("Company soft-deleted: id={}", id);
    }

    private void applyRequest(Company company, CreateCompanyRequest r) {
        company.setName(r.getName());
        company.setLegalName(r.getLegalName());
        company.setRegistrationNumber(r.getRegistrationNumber());
        company.setTaxNumber(r.getTaxNumber());
        company.setEmail(r.getEmail());
        company.setPhone(r.getPhone());
        company.setWebsite(r.getWebsite());
        company.setAddress(r.getAddress());
        company.setCity(r.getCity());
        company.setState(r.getState());
        company.setCountry(r.getCountry());
        company.setPostalCode(r.getPostalCode());
        company.setGstin(r.getGstin());
        company.setPan(r.getPan());
        company.setCin(r.getCin());
        company.setTan(r.getTan());
        company.setBankName(r.getBankName());
        company.setBankAccount(r.getBankAccount());
        company.setBankIFSC(r.getBankIFSC());
        company.setBankBranch(r.getBankBranch());
        if (r.getFinancialYearStart() != null) company.setFinancialYearStart(r.getFinancialYearStart());
        // Whichever name it arrived under. The screen sends logo on a fresh upload and logoUrl
        // on a round-trip, and taking only one of them silently dropped the other.
        if (r.getLogo() != null) company.setLogo(r.getLogo());
        else if (r.getLogoUrl() != null) company.setLogo(r.getLogoUrl());
        if (r.getPdfWatermarkEnabled() != null) company.setPdfWatermarkEnabled(r.getPdfWatermarkEnabled());
        company.setIndustry(r.getIndustry());
        if (r.getCurrency() != null) company.setCurrency(r.getCurrency());
    }

    private CompanyDto toDto(Company c) {
        CompanyDto dto = new CompanyDto();
        dto.setId(c.getId());
        dto.setTenantId(c.getTenantId());
        dto.setName(c.getName());
        dto.setLegalName(c.getLegalName());
        dto.setRegistrationNumber(c.getRegistrationNumber());
        dto.setTaxNumber(c.getTaxNumber());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setWebsite(c.getWebsite());
        dto.setAddress(c.getAddress());
        dto.setCity(c.getCity());
        dto.setState(c.getState());
        dto.setCountry(c.getCountry());
        dto.setPostalCode(c.getPostalCode());
        dto.setLogo(c.getLogo());
        dto.setLogoUrl(c.getLogo());
        dto.setPdfWatermarkEnabled(c.isPdfWatermarkEnabled());
        dto.setGstin(c.getGstin());
        dto.setPan(c.getPan());
        dto.setCin(c.getCin());
        dto.setTan(c.getTan());
        dto.setBankName(c.getBankName());
        dto.setBankAccount(c.getBankAccount());
        dto.setBankIFSC(c.getBankIFSC());
        dto.setBankBranch(c.getBankBranch());
        dto.setFinancialYearStart(c.getFinancialYearStart());
        dto.setIndustry(c.getIndustry());
        dto.setCurrency(c.getCurrency());
        dto.setActive(c.isActive());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }

    private Company findOrThrow(UUID id) {
        return companyRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Company not found: " + id));
    }
}

package com.erp.platform.modules.agri.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.agri.dto.CreateTestLocationRequest;
import com.erp.platform.modules.agri.dto.TestLocationDto;
import com.erp.platform.modules.agri.entity.TestLocation;
import com.erp.platform.modules.agri.repository.TestLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestLocationService {

    private final TestLocationRepository testLocationRepository;
    private final TenantContext tenantContext;

    public PageResponse<TestLocationDto> list(Pageable pageable) {
        return PageResponse.of(testLocationRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    public TestLocationDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        TestLocation entity = testLocationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("TestLocation not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public TestLocationDto create(CreateTestLocationRequest request) {
        UUID tenantId = tenantContext.current();
        TestLocation entity = new TestLocation();
        entity.setTenantId(tenantId);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setActive(request.isActive());
        return toDto(testLocationRepository.save(entity));
    }

    @Transactional
    public TestLocationDto update(UUID id, CreateTestLocationRequest request) {
        UUID tenantId = tenantContext.current();
        TestLocation entity = testLocationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("TestLocation not found: " + id));
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setActive(request.isActive());
        return toDto(testLocationRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        TestLocation entity = testLocationRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("TestLocation not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        testLocationRepository.save(entity);
    }

    private TestLocationDto toDto(TestLocation entity) {
        TestLocationDto dto = new TestLocationDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}


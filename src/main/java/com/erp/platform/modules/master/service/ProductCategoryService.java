package com.erp.platform.modules.master.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateProductCategoryRequest;
import com.erp.platform.modules.master.dto.ProductCategoryDto;
import com.erp.platform.modules.master.entity.ProductCategory;
import com.erp.platform.modules.master.mapper.ProductCategoryMapper;
import com.erp.platform.modules.master.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductCategoryMapper categoryMapper;
    private final TenantContext tenantContext;

    public PageResponse<ProductCategoryDto> list(Pageable pageable) {
        UUID tenantId = tenantContext.current();
        List<ProductCategory> categories = categoryRepository.findByTenantIdAndActiveTrueAndDeletedAtIsNull(tenantId);
        List<ProductCategoryDto> dtos = categories.stream().map(categoryMapper::toDto).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<ProductCategoryDto> page = start > dtos.size() ? List.of() : dtos.subList(start, end);
        return PageResponse.of(new PageImpl<>(page, pageable, dtos.size()));
    }

    public ProductCategoryDto getById(UUID id) {
        return categoryMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public ProductCategoryDto create(CreateProductCategoryRequest request) {
        UUID tenantId = tenantContext.current();
        ProductCategory category = categoryMapper.toEntity(request);
        category.setTenantId(tenantId);
        category.setActive(true);
        if (request.getParentId() != null) {
            ProductCategory parent = findOrThrow(request.getParentId());
            category.setParent(parent);
        }
        category = categoryRepository.save(category);
        log.info("ProductCategory created: id={}, name={}", category.getId(), category.getName());
        return categoryMapper.toDto(category);
    }

    @Transactional
    public ProductCategoryDto update(UUID id, CreateProductCategoryRequest request) {
        ProductCategory category = findOrThrow(id);
        category.setName(request.getName());
        if (request.getCode() != null) category.setCode(request.getCode());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getParentId() != null) {
            ProductCategory parent = findOrThrow(request.getParentId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        ProductCategory category = findOrThrow(id);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
        log.info("ProductCategory soft-deleted: id={}", id);
    }

    private ProductCategory findOrThrow(UUID id) {
        return categoryRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Product category not found: " + id));
    }
}

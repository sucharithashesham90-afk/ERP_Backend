package com.erp.platform.modules.promotions.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.promotions.dto.CreatePromotionRequest;
import com.erp.platform.modules.promotions.dto.PromotionDto;
import com.erp.platform.modules.promotions.dto.PromotionRedemptionDto;
import com.erp.platform.modules.promotions.entity.Promotion;
import com.erp.platform.modules.promotions.entity.PromotionProduct;
import com.erp.platform.modules.promotions.entity.PromotionRedemption;
import com.erp.platform.modules.promotions.repository.PromotionRedemptionRepository;
import com.erp.platform.modules.promotions.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository redemptionRepository;
    private final TenantContext tenantContext;

    public PageResponse<PromotionDto> list(boolean activeOnly, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = activeOnly
                ? promotionRepository.findByTenantIdAndActiveAndDeletedAtIsNull(tenantId, true, pageable)
                : promotionRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public PromotionDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    public List<PromotionDto> getActivePromotions(LocalDate date) {
        UUID tenantId = tenantContext.current();
        return promotionRepository
                .findByTenantIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndActiveAndDeletedAtIsNull(
                        tenantId, date, date, true)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public PromotionDto create(CreatePromotionRequest request) {
        UUID tenantId = tenantContext.current();

        Promotion promotion = new Promotion();
        promotion.setTenantId(tenantId);
        promotion.setPromotionCode(generatePromotionCode(tenantId));
        mapRequestToPromotion(request, promotion, tenantId);

        promotion = promotionRepository.save(promotion);
        log.info("Promotion created: id={}, code={}", promotion.getId(), promotion.getPromotionCode());
        return toDto(promotion);
    }

    @Transactional
    public PromotionDto update(UUID id, CreatePromotionRequest request) {
        Promotion promotion = findOrThrow(id);
        mapRequestToPromotion(request, promotion, promotion.getTenantId());
        promotion = promotionRepository.save(promotion);
        log.info("Promotion updated: id={}", promotion.getId());
        return toDto(promotion);
    }

    @Transactional
    public PromotionDto activate(UUID id) {
        Promotion promotion = findOrThrow(id);
        promotion.setActive(true);
        return toDto(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionDto deactivate(UUID id) {
        Promotion promotion = findOrThrow(id);
        promotion.setActive(false);
        return toDto(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionRedemptionDto recordRedemption(UUID promotionId, UUID salesOrderId, UUID customerId, BigDecimal discount) {
        Promotion promotion = findOrThrow(promotionId);

        if (promotion.getUsageLimit() > 0 && promotion.getUsageCount() >= promotion.getUsageLimit()) {
            throw AppException.businessRule("Promotion usage limit reached");
        }

        promotion.setUsageCount(promotion.getUsageCount() + 1);
        promotionRepository.save(promotion);

        PromotionRedemption redemption = new PromotionRedemption();
        redemption.setTenantId(promotion.getTenantId());
        redemption.setPromotion(promotion);
        redemption.setSalesOrderId(salesOrderId);
        redemption.setCustomerId(customerId);
        redemption.setRedemptionDate(LocalDate.now());
        redemption.setDiscountApplied(discount);
        redemption = redemptionRepository.save(redemption);

        log.info("Promotion redemption recorded: promotionId={}, salesOrderId={}", promotionId, salesOrderId);
        return toRedemptionDto(redemption);
    }

    @Transactional
    public void delete(UUID id) {
        Promotion promotion = findOrThrow(id);
        promotion.setDeletedAt(LocalDateTime.now());
        promotionRepository.save(promotion);
        log.info("Promotion soft-deleted: id={}", id);
    }

    private void mapRequestToPromotion(CreatePromotionRequest request, Promotion promotion, UUID tenantId) {
        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setPromotionType(request.getPromotionType());
        promotion.setApplicableTo(request.getApplicableTo());
        promotion.setCustomerCategory(request.getCustomerCategory());
        promotion.setDiscountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO);
        promotion.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        promotion.setMinOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : BigDecimal.ZERO);
        promotion.setMinOrderQty(request.getMinOrderQty() != null ? request.getMinOrderQty() : BigDecimal.ZERO);
        promotion.setBuyQuantity(request.getBuyQuantity());
        promotion.setGetQuantity(request.getGetQuantity());
        promotion.setFreeProductId(request.getFreeProductId());
        promotion.setFreeProductName(request.getFreeProductName());
        promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setUsageLimit(request.getUsageLimit());
        promotion.setStackable(request.isStackable());
        promotion.setActive(request.isActive());
        promotion.setNotes(request.getNotes());

        promotion.getApplicableProducts().clear();
        if (request.getApplicableProducts() != null) {
            List<PromotionProduct> products = request.getApplicableProducts().stream().map(r -> {
                PromotionProduct pp = new PromotionProduct();
                pp.setTenantId(tenantId);
                pp.setPromotion(promotion);
                pp.setProductId(r.getProductId());
                pp.setProductName(r.getProductName());
                pp.setMinQuantity(r.getMinQuantity());
                pp.setDiscountPercent(r.getDiscountPercent());
                return pp;
            }).collect(Collectors.toList());
            promotion.getApplicableProducts().addAll(products);
        }
    }

    private PromotionDto toDto(Promotion p) {
        PromotionDto dto = new PromotionDto();
        dto.setId(p.getId());
        dto.setTenantId(p.getTenantId());
        dto.setPromotionCode(p.getPromotionCode());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPromotionType(p.getPromotionType());
        dto.setApplicableTo(p.getApplicableTo());
        dto.setCustomerCategory(p.getCustomerCategory());
        dto.setDiscountPercent(p.getDiscountPercent());
        dto.setDiscountAmount(p.getDiscountAmount());
        dto.setMinOrderValue(p.getMinOrderValue());
        dto.setMinOrderQty(p.getMinOrderQty());
        dto.setBuyQuantity(p.getBuyQuantity());
        dto.setGetQuantity(p.getGetQuantity());
        dto.setFreeProductId(p.getFreeProductId());
        dto.setFreeProductName(p.getFreeProductName());
        dto.setMaxDiscountAmount(p.getMaxDiscountAmount());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setUsageLimit(p.getUsageLimit());
        dto.setUsageCount(p.getUsageCount());
        dto.setStackable(p.isStackable());
        dto.setActive(p.isActive());
        dto.setNotes(p.getNotes());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        if (p.getApplicableProducts() != null) {
            dto.setApplicableProducts(p.getApplicableProducts().stream().map(pp -> {
                PromotionDto.ProductDto pdto = new PromotionDto.ProductDto();
                pdto.setId(pp.getId());
                pdto.setProductId(pp.getProductId());
                pdto.setProductName(pp.getProductName());
                pdto.setMinQuantity(pp.getMinQuantity());
                pdto.setDiscountPercent(pp.getDiscountPercent());
                return pdto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private PromotionRedemptionDto toRedemptionDto(PromotionRedemption r) {
        PromotionRedemptionDto dto = new PromotionRedemptionDto();
        dto.setId(r.getId());
        dto.setTenantId(r.getTenantId());
        dto.setPromotionId(r.getPromotion().getId());
        dto.setPromotionCode(r.getPromotion().getPromotionCode());
        dto.setPromotionName(r.getPromotion().getName());
        dto.setSalesOrderId(r.getSalesOrderId());
        dto.setCustomerId(r.getCustomerId());
        dto.setCustomerName(r.getCustomerName());
        dto.setRedemptionDate(r.getRedemptionDate());
        dto.setDiscountApplied(r.getDiscountApplied());
        dto.setNotes(r.getNotes());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    private Promotion findOrThrow(UUID id) {
        return promotionRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Promotion not found: " + id));
    }

    private String generatePromotionCode(UUID tenantId) {
        long count = promotionRepository.countByTenantId(tenantId);
        String year = String.valueOf(Year.now().getValue());
        return String.format("PROMO-%s-%03d", year, count + 1);
    }
}

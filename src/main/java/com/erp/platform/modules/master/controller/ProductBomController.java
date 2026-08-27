package com.erp.platform.modules.master.controller;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.modules.master.dto.CreateProductBomLineRequest;
import com.erp.platform.modules.master.dto.ProductBomLineDto;
import com.erp.platform.modules.master.service.ProductBomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/master/product-bom")
@RequiredArgsConstructor
public class ProductBomController {

    private final ProductBomService service;

    @GetMapping("/{productId}/lines")
    public ApiResponse<List<ProductBomLineDto>> list(@PathVariable UUID productId) {
        return ApiResponse.success(service.listByProduct(productId));
    }

    @GetMapping("/brand/{brandId}/lines")
    public ApiResponse<List<ProductBomLineDto>> listByBrand(@PathVariable UUID brandId) {
        return ApiResponse.success(service.listByBrand(brandId));
    }

    @PostMapping
    public ApiResponse<ProductBomLineDto> create(@RequestBody CreateProductBomLineRequest req) {
        return ApiResponse.success(service.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductBomLineDto> update(@PathVariable UUID id,
                                                  @RequestBody CreateProductBomLineRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}

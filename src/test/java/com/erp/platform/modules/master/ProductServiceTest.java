package com.erp.platform.modules.master;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateProductRequest;
import com.erp.platform.modules.master.dto.ProductDto;
import com.erp.platform.modules.master.dto.UpdateProductRequest;
import com.erp.platform.modules.master.entity.Product;
import com.erp.platform.modules.master.entity.Product.ProductType;
import com.erp.platform.modules.master.mapper.ProductMapper;
import com.erp.platform.modules.master.repository.ProductCategoryRepository;
import com.erp.platform.modules.master.repository.ProductRepository;
import com.erp.platform.modules.master.repository.TaxRepository;
import com.erp.platform.modules.master.service.ProductService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService unit tests")
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductCategoryRepository categoryRepository;
    @Mock private TaxRepository taxRepository;
    @Mock private ProductMapper productMapper;
    @Mock private TenantContext tenantContext;
    @InjectMocks private ProductService productService;

    private static final UUID TENANT_ID  = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @BeforeEach void setUp() { when(tenantContext.current()).thenReturn(TENANT_ID); }

    @Test
    @DisplayName("list() returns paginated products")
    void list_returnsPaginatedResults() {
        Product p = product();
        when(productRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of(p)));
        when(productMapper.toDto(p)).thenReturn(new ProductDto());

        PageResponse<ProductDto> result = productService.list(null, null, PageRequest.of(0, 20));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND for non-existent product")
    void getById_throwsNotFound() {
        when(productRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> productService.getById(PRODUCT_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("create() throws CONFLICT on duplicate product code")
    void create_throwsOnDuplicateCode() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Product A");
        request.setCode("PROD-001");
        request.setProductType(ProductType.GOODS);
        request.setPurchasePrice(BigDecimal.TEN);
        request.setSalePrice(BigDecimal.valueOf(15));

        when(productRepository.existsByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "PROD-001"))
                .thenReturn(true);

        AppException ex = Assertions.assertThrows(AppException.class,
                () -> productService.create(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("create() saves product with tenantId")
    void create_setsTenantId() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("New Product");
        request.setProductType(ProductType.GOODS);
        request.setPurchasePrice(BigDecimal.TEN);
        request.setSalePrice(BigDecimal.valueOf(15));

        Product entity = product();
        // No code in request — existsByTenantIdAndCodeAndDeletedAtIsNull is skipped
        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.save(any())).thenReturn(entity);
        when(productMapper.toDto(entity)).thenReturn(new ProductDto());

        productService.create(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    @DisplayName("delete() soft-deletes product")
    void delete_setsDeletedAt() {
        Product p = product();
        when(productRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, PRODUCT_ID))
                .thenReturn(Optional.of(p));
        when(productRepository.save(any())).thenReturn(p);

        productService.delete(PRODUCT_ID);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    private Product product() {
        Product p = new Product();
        p.setId(PRODUCT_ID);
        p.setTenantId(TENANT_ID);
        p.setName("Test Product");
        p.setCode("PROD-001");
        p.setProductType(ProductType.GOODS);
        p.setSalePrice(BigDecimal.valueOf(100));
        p.setPurchasePrice(BigDecimal.valueOf(75));
        p.setActive(true);
        return p;
    }
}

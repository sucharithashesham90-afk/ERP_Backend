package com.erp.platform.modules.master;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateVendorRequest;
import com.erp.platform.modules.master.dto.UpdateVendorRequest;
import com.erp.platform.modules.master.dto.VendorDto;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.master.mapper.VendorMapper;
import com.erp.platform.modules.master.repository.VendorRepository;
import com.erp.platform.modules.master.service.VendorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendorService unit tests")
class VendorServiceTest {

    @Mock private VendorRepository vendorRepository;
    @Mock private VendorMapper vendorMapper;
    @Mock private TenantContext tenantContext;
    @InjectMocks private VendorService vendorService;

    private static final UUID TENANT_ID  = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID VENDOR_ID  = UUID.randomUUID();

    @BeforeEach void setUp() { when(tenantContext.current()).thenReturn(TENANT_ID); }

    @Test
    @DisplayName("list() returns page of vendors without search")
    void list_returnsPage() {
        Vendor v = TestDataBuilder.vendor().build();
        VendorDto dto = new VendorDto();
        Page<Vendor> page = new PageImpl<>(List.of(v));

        when(vendorRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any())).thenReturn(page);
        when(vendorMapper.toDto(v)).thenReturn(dto);

        PageResponse<VendorDto> result = vendorService.list(null, PageRequest.of(0, 20));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("getById() returns DTO for existing vendor")
    void getById_returnsDto() {
        Vendor v = TestDataBuilder.vendor().id(VENDOR_ID).build();
        VendorDto dto = new VendorDto();
        dto.setId(VENDOR_ID);

        when(vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, VENDOR_ID)).thenReturn(Optional.of(v));
        when(vendorMapper.toDto(v)).thenReturn(dto);

        assertThat(vendorService.getById(VENDOR_ID).getId()).isEqualTo(VENDOR_ID);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND for missing vendor")
    void getById_throwsNotFound() {
        when(vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, VENDOR_ID)).thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class, () -> vendorService.getById(VENDOR_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("create() saves vendor with tenantId and auto-generates code")
    void create_savesWithTenantId() {
        CreateVendorRequest request = TestDataBuilder.createVendorRequest();
        Vendor entity = new Vendor();

        when(vendorRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(any(), any())).thenReturn(false);
        when(vendorMapper.toEntity(request)).thenReturn(entity);
        when(vendorRepository.save(any())).thenReturn(entity);
        when(vendorMapper.toDto(entity)).thenReturn(new VendorDto());

        vendorService.create(request);

        ArgumentCaptor<Vendor> captor = ArgumentCaptor.forClass(Vendor.class);
        verify(vendorRepository).save(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(TENANT_ID);
        assertThat(captor.getValue().getCode()).startsWith("VEND-");
    }

    @Test
    @DisplayName("create() throws CONFLICT on duplicate email")
    void create_throwsConflictOnDuplicateEmail() {
        CreateVendorRequest request = TestDataBuilder.createVendorRequest();
        when(vendorRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(TENANT_ID, request.getEmail())).thenReturn(true);

        AppException ex = Assertions.assertThrows(AppException.class, () -> vendorService.create(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete() sets deletedAt on vendor (soft delete)")
    void delete_setsDeletedAt() {
        Vendor v = TestDataBuilder.vendor().id(VENDOR_ID).build();
        when(vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, VENDOR_ID)).thenReturn(Optional.of(v));
        when(vendorRepository.save(any())).thenReturn(v);

        vendorService.delete(VENDOR_ID);

        ArgumentCaptor<Vendor> captor = ArgumentCaptor.forClass(Vendor.class);
        verify(vendorRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("delete() throws NOT_FOUND for missing vendor")
    void delete_throwsNotFound() {
        when(vendorRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, VENDOR_ID)).thenReturn(Optional.empty());
        Assertions.assertThrows(AppException.class, () -> vendorService.delete(VENDOR_ID));
        verify(vendorRepository, never()).save(any());
    }
}

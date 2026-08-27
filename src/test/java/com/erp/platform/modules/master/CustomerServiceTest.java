package com.erp.platform.modules.master;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.master.dto.CreateCustomerRequest;
import com.erp.platform.modules.master.dto.CustomerDto;
import com.erp.platform.modules.master.dto.UpdateCustomerRequest;
import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.entity.Customer.CustomerCategory;
import com.erp.platform.modules.master.mapper.CustomerMapper;
import com.erp.platform.modules.master.repository.CustomerRepository;
import com.erp.platform.modules.master.service.CustomerService;
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
@DisplayName("CustomerService unit tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private TenantContext tenantContext;

    @InjectMocks
    private CustomerService customerService;

    private static final UUID TENANT_ID   = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(tenantContext.current()).thenReturn(TENANT_ID);
    }

    // ─── list ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("list()")
    class ListCustomers {

        @Test
        @DisplayName("returns paginated results when no search term")
        void shouldReturnPageWhenNoSearch() {
            Customer c = TestDataBuilder.customer().build();
            CustomerDto dto = new CustomerDto();
            dto.setId(c.getId());

            Page<Customer> page = new PageImpl<>(List.of(c), PageRequest.of(0, 20), 1);

            when(customerRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                    .thenReturn(page);
            when(customerMapper.toDto(c)).thenReturn(dto);

            PageResponse<CustomerDto> result = customerService.list(null, PageRequest.of(0, 20));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(customerRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
            verify(customerRepository, never()).searchByTenantId(any(), any(), any());
        }

        @Test
        @DisplayName("delegates to search query when search term is provided")
        void shouldUseSearchQueryWhenSearchProvided() {
            Page<Customer> emptyPage = new PageImpl<>(List.of());
            when(customerRepository.searchByTenantId(eq(TENANT_ID), eq("Acme"), any()))
                    .thenReturn(emptyPage);

            customerService.list("Acme", PageRequest.of(0, 20));

            verify(customerRepository).searchByTenantId(eq(TENANT_ID), eq("Acme"), any());
            verify(customerRepository, never()).findByTenantIdAndDeletedAtIsNull(any(), any());
        }

        @Test
        @DisplayName("returns empty page when no customers exist")
        void shouldReturnEmptyPage() {
            Page<Customer> emptyPage = new PageImpl<>(List.of());
            when(customerRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                    .thenReturn(emptyPage);

            PageResponse<CustomerDto> result = customerService.list(null, PageRequest.of(0, 20));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns DTO when customer exists")
        void shouldReturnCustomerDto() {
            Customer c = TestDataBuilder.customer().id(CUSTOMER_ID).build();
            CustomerDto dto = new CustomerDto();
            dto.setId(CUSTOMER_ID);

            when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                    .thenReturn(Optional.of(c));
            when(customerMapper.toDto(c)).thenReturn(dto);

            CustomerDto result = customerService.getById(CUSTOMER_ID);

            assertThat(result.getId()).isEqualTo(CUSTOMER_ID);
        }

        @Test
        @DisplayName("throws NOT_FOUND when customer doesn't exist")
        void shouldThrowWhenNotFound() {
            when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                    .thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class,
                    () -> customerService.getById(CUSTOMER_ID));

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        }

        @Test
        @DisplayName("throws NOT_FOUND when customer belongs to different tenant")
        void shouldThrowForDifferentTenant() {
            UUID otherTenant = UUID.randomUUID();
            when(tenantContext.current()).thenReturn(otherTenant);
            when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(otherTenant, CUSTOMER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class, () -> customerService.getById(CUSTOMER_ID));
        }
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class CreateCustomer {

        @Test
        @DisplayName("creates customer successfully with valid data")
        void shouldCreateSuccessfully() {
            CreateCustomerRequest request = TestDataBuilder.createCustomerRequest();
            Customer entity = TestDataBuilder.customer().build();
            CustomerDto dto = new CustomerDto();
            dto.setId(entity.getId());
            dto.setName(entity.getName());

            when(customerRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(TENANT_ID, request.getEmail()))
                    .thenReturn(false);
            when(customerMapper.toEntity(request)).thenReturn(entity);
            when(customerRepository.save(any(Customer.class))).thenReturn(entity);
            when(customerMapper.toDto(entity)).thenReturn(dto);

            CustomerDto result = customerService.create(request);

            assertThat(result).isNotNull();
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("sets tenantId from context on new customer")
        void shouldSetTenantIdFromContext() {
            CreateCustomerRequest request = TestDataBuilder.createCustomerRequest();
            Customer entity = new Customer();

            when(customerRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(any(), any()))
                    .thenReturn(false);
            when(customerMapper.toEntity(request)).thenReturn(entity);
            when(customerRepository.save(any())).thenReturn(entity);
            when(customerMapper.toDto(entity)).thenReturn(new CustomerDto());

            customerService.create(request);

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());
            assertThat(captor.getValue().getTenantId()).isEqualTo(TENANT_ID);
        }

        @Test
        @DisplayName("throws CONFLICT when email already exists in tenant")
        void shouldThrowConflictOnDuplicateEmail() {
            CreateCustomerRequest request = TestDataBuilder.createCustomerRequest();

            when(customerRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(TENANT_ID, request.getEmail()))
                    .thenReturn(true);

            AppException ex = assertThrows(AppException.class,
                    () -> customerService.create(request));

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("auto-generates code when code is blank")
        void shouldAutoGenerateCodeWhenBlank() {
            CreateCustomerRequest request = TestDataBuilder.createCustomerRequest();
            Customer entity = new Customer();
            entity.setCode(null);

            when(customerRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(any(), any()))
                    .thenReturn(false);
            when(customerMapper.toEntity(request)).thenReturn(entity);
            when(customerRepository.save(any())).thenReturn(entity);
            when(customerMapper.toDto(entity)).thenReturn(new CustomerDto());

            customerService.create(request);

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());
            assertThat(captor.getValue().getCode()).startsWith("CUST-");
        }

        @Test
        @DisplayName("does not check email uniqueness when email is blank")
        void shouldSkipEmailCheckWhenEmailBlank() {
            CreateCustomerRequest request = new CreateCustomerRequest();
            request.setName("Customer Without Email");
            request.setCategory(CustomerCategory.RETAIL);
            request.setCreditLimit(BigDecimal.ZERO);

            Customer entity = new Customer();
            when(customerMapper.toEntity(request)).thenReturn(entity);
            when(customerRepository.save(any())).thenReturn(entity);
            when(customerMapper.toDto(entity)).thenReturn(new CustomerDto());

            customerService.create(request);

            verify(customerRepository, never())
                    .existsByTenantIdAndEmailAndDeletedAtIsNull(any(), any());
        }
    }

    // ─── update ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class UpdateCustomer {

        @Test
        @DisplayName("updates customer successfully when found")
        void shouldUpdateSuccessfully() {
            Customer entity = TestDataBuilder.customer().id(CUSTOMER_ID).build();
            UpdateCustomerRequest request = new UpdateCustomerRequest();
            request.setName("Updated Name");
            CustomerDto dto = new CustomerDto();

            when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                    .thenReturn(Optional.of(entity));
            when(customerRepository.save(entity)).thenReturn(entity);
            when(customerMapper.toDto(entity)).thenReturn(dto);

            CustomerDto result = customerService.update(CUSTOMER_ID, request);

            assertThat(result).isNotNull();
            verify(customerMapper).updateEntity(request, entity);
            verify(customerRepository).save(entity);
        }

        @Test
        @DisplayName("throws NOT_FOUND when customer to update doesn't exist")
        void shouldThrowWhenUpdateTargetNotFound() {
            when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class,
                    () -> customerService.update(CUSTOMER_ID, new UpdateCustomerRequest()));
        }
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete()")
    class DeleteCustomer {

        @Test
        @DisplayName("soft-deletes customer by setting deletedAt")
        void shouldSoftDeleteCustomer() {
            Customer entity = TestDataBuilder.customer().id(CUSTOMER_ID).build();

            when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                    .thenReturn(Optional.of(entity));
            when(customerRepository.save(any())).thenReturn(entity);

            customerService.delete(CUSTOMER_ID);

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());
            assertThat(captor.getValue().getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("throws NOT_FOUND when customer to delete doesn't exist")
        void shouldThrowWhenDeleteTargetNotFound() {
            when(customerRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID))
                    .thenReturn(Optional.empty());

            assertThrows(AppException.class, () -> customerService.delete(CUSTOMER_ID));
            verify(customerRepository, never()).save(any());
        }
    }

    private static AppException assertThrows(Class<AppException> exClass, org.junit.jupiter.api.function.Executable exec) {
        return Assertions.assertThrows(exClass, exec);
    }
}

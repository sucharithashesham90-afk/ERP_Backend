package com.erp.platform.modules.hr;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.hr.dto.CreateEmployeeRequest;
import com.erp.platform.modules.hr.dto.EmployeeDto;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.entity.Employee.EmployeeStatus;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import com.erp.platform.modules.hr.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService unit tests")
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private EmployeeService employeeService;

    private static final UUID TENANT_ID   = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();

    @BeforeEach void setUp() { when(tenantContext.current()).thenReturn(TENANT_ID); }

    // create() only provisions a login when the request supplies both managerId and
    // defaultPassword (see EmployeeService.create()) — neither test below sets them, so the new
    // UserRepository/RoleRepository/PasswordEncoder path is never exercised and needs no mocking.

    // ─── list ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list() returns employees without search")
    void list_withoutSearch() {
        Employee e = employee();
        Page<Employee> page = new PageImpl<>(List.of(e));

        when(employeeRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(page);

        PageResponse<EmployeeDto> result = employeeService.list(null, PageRequest.of(0, 20));
        assertThat(result.getContent()).hasSize(1);
        verify(employeeRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
    }

    @Test
    @DisplayName("list() uses search query when search provided")
    void list_withSearch() {
        when(employeeRepository.searchByTenantId(eq(TENANT_ID), eq("John"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        employeeService.list("John", PageRequest.of(0, 20));
        verify(employeeRepository).searchByTenantId(eq(TENANT_ID), eq("John"), any());
        verify(employeeRepository, never()).findByTenantIdAndDeletedAtIsNull(any(), any());
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById() returns DTO for existing employee")
    void getById_success() {
        Employee e = employee();
        when(employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, EMPLOYEE_ID))
                .thenReturn(Optional.of(e));

        EmployeeDto dto = employeeService.getById(EMPLOYEE_ID);
        assertThat(dto.getId()).isEqualTo(EMPLOYEE_ID);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND for missing employee")
    void getById_notFound() {
        when(employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        AppException ex = Assertions.assertThrows(AppException.class,
                () -> employeeService.getById(EMPLOYEE_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() sets tenantId and ACTIVE status")
    void create_setsTenantIdAndStatus() {
        CreateEmployeeRequest request = createRequest();
        Employee saved = employee();

        when(employeeRepository.count()).thenReturn(0L);
        when(employeeRepository.save(any())).thenReturn(saved);

        employeeService.create(request);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(TENANT_ID);
        assertThat(captor.getValue().getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

    @Test
    @DisplayName("create() auto-generates employee code when none provided")
    void create_autoGeneratesCode() {
        CreateEmployeeRequest request = createRequest();
        // CreateEmployeeRequest has no employeeCode field — code is always auto-generated
        Employee saved = employee();

        when(employeeRepository.count()).thenReturn(5L);
        when(employeeRepository.save(any())).thenReturn(saved);

        employeeService.create(request);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getEmployeeCode()).startsWith("EMP-");
    }

    // ─── delete (soft delete) ─────────────────────────────────────────────────

    @Test
    @DisplayName("delete() soft-deletes employee by setting deletedAt")
    void delete_setsDeletedAt() {
        Employee e = employee();
        when(employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, EMPLOYEE_ID))
                .thenReturn(Optional.of(e));
        when(employeeRepository.save(any())).thenReturn(e);

        employeeService.delete(EMPLOYEE_ID);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("delete() throws NOT_FOUND for missing employee")
    void delete_throwsNotFound() {
        when(employeeRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, EMPLOYEE_ID))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(AppException.class, () -> employeeService.delete(EMPLOYEE_ID));
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private Employee employee() {
        Employee e = new Employee();
        e.setId(EMPLOYEE_ID);
        e.setTenantId(TENANT_ID);
        e.setEmployeeCode("EMP-2026-0001");
        e.setFirstName("John");
        e.setLastName("Doe");
        e.setEmail("john.doe@company.com");
        e.setStatus(EmployeeStatus.ACTIVE);
        e.setJoiningDate(LocalDate.now().minusYears(1));
        e.setBasicSalary(BigDecimal.valueOf(50000));
        return e;
    }

    private CreateEmployeeRequest createRequest() {
        CreateEmployeeRequest r = new CreateEmployeeRequest();
        r.setFirstName("Jane");
        r.setLastName("Smith");
        r.setEmail("jane.smith@company.com");
        r.setJoiningDate(LocalDate.now());
        r.setBasicSalary(BigDecimal.valueOf(40000));
        return r;
    }
}

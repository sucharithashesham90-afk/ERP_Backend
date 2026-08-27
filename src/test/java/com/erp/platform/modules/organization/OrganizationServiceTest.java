package com.erp.platform.modules.organization;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.organization.dto.BranchDto;
import com.erp.platform.modules.organization.dto.CreateBranchRequest;
import com.erp.platform.modules.organization.entity.Branch;
import com.erp.platform.modules.organization.repository.BranchRepository;
import com.erp.platform.modules.organization.service.BranchService;
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
@DisplayName("BranchService unit tests")
class OrganizationServiceTest {

    @Mock private BranchRepository branchRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private BranchService branchService;

    private static final UUID TENANT_ID  = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID BRANCH_ID  = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(tenantContext.current()).thenReturn(TENANT_ID);
    }

    // ─── list ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list() without companyId returns all branches for tenant")
    void list_noCompanyFilter() {
        when(branchRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        branchService.list(null, PageRequest.of(0, 20));
        verify(branchRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
    }

    @Test
    @DisplayName("list() with companyId filters by company")
    void list_withCompanyFilter() {
        when(branchRepository.findByTenantIdAndCompanyIdAndDeletedAtIsNull(
                eq(TENANT_ID), eq(COMPANY_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        branchService.list(COMPANY_ID, PageRequest.of(0, 20));
        verify(branchRepository).findByTenantIdAndCompanyIdAndDeletedAtIsNull(
                eq(TENANT_ID), eq(COMPANY_ID), any());
    }

    // ─── getById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById() returns DTO for existing branch")
    void getById_found() {
        Branch branch = buildBranch();
        when(branchRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, BRANCH_ID))
                .thenReturn(Optional.of(branch));
        BranchDto dto = branchService.getById(BRANCH_ID);
        assertThat(dto.getId()).isEqualTo(BRANCH_ID);
        assertThat(dto.getName()).isEqualTo("Head Office");
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND for missing branch")
    void getById_notFound() {
        when(branchRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, BRANCH_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> branchService.getById(BRANCH_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() saves branch with tenantId from context")
    void create_setsTenanId() {
        CreateBranchRequest req = buildRequest();
        when(branchRepository.save(any())).thenAnswer(i -> {
            Branch saved = i.getArgument(0);
            saved.setId(BRANCH_ID);
            return saved;
        });

        BranchDto dto = branchService.create(req);

        ArgumentCaptor<Branch> captor = ArgumentCaptor.forClass(Branch.class);
        verify(branchRepository).save(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(TENANT_ID);
        assertThat(dto.getName()).isEqualTo("Head Office");
    }

    @Test
    @DisplayName("create() sets companyId from request")
    void create_setsCompanyId() {
        CreateBranchRequest req = buildRequest();
        when(branchRepository.save(any())).thenAnswer(i -> {
            Branch saved = i.getArgument(0);
            saved.setId(BRANCH_ID);
            return saved;
        });

        branchService.create(req);

        ArgumentCaptor<Branch> captor = ArgumentCaptor.forClass(Branch.class);
        verify(branchRepository).save(captor.capture());
        assertThat(captor.getValue().getCompanyId()).isEqualTo(COMPANY_ID);
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update() saves updated fields")
    void update_success() {
        Branch existing = buildBranch();
        when(branchRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, BRANCH_ID))
                .thenReturn(Optional.of(existing));
        when(branchRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CreateBranchRequest req = buildRequest();
        req.setName("Mumbai Branch");
        req.setCity("Mumbai");

        BranchDto dto = branchService.update(BRANCH_ID, req);
        assertThat(dto.getName()).isEqualTo("Mumbai Branch");
        assertThat(dto.getCity()).isEqualTo("Mumbai");
    }

    @Test
    @DisplayName("update() throws NOT_FOUND for missing branch")
    void update_notFound() {
        when(branchRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, BRANCH_ID))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(AppException.class,
                () -> branchService.update(BRANCH_ID, buildRequest()));
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() soft-deletes branch")
    void delete_setsDeletedAt() {
        Branch branch = buildBranch();
        when(branchRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, BRANCH_ID))
                .thenReturn(Optional.of(branch));
        when(branchRepository.save(any())).thenReturn(branch);

        branchService.delete(BRANCH_ID);

        ArgumentCaptor<Branch> captor = ArgumentCaptor.forClass(Branch.class);
        verify(branchRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("delete() throws NOT_FOUND for missing branch")
    void delete_notFound() {
        when(branchRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, BRANCH_ID))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(AppException.class, () -> branchService.delete(BRANCH_ID));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Branch buildBranch() {
        Branch b = new Branch();
        b.setId(BRANCH_ID);
        b.setTenantId(TENANT_ID);
        b.setCompanyId(COMPANY_ID);
        b.setName("Head Office");
        b.setCode("HO");
        b.setCity("Hyderabad");
        b.setState("Telangana");
        b.setHeadOffice(true);
        b.setActive(true);
        return b;
    }

    private CreateBranchRequest buildRequest() {
        CreateBranchRequest r = new CreateBranchRequest();
        r.setCompanyId(COMPANY_ID);
        r.setName("Head Office");
        r.setCode("HO");
        r.setCity("Hyderabad");
        r.setState("Telangana");
        r.setHeadOffice(true);
        return r;
    }
}

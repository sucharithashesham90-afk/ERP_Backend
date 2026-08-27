package com.erp.platform.modules.accounting;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.service.AccountService;
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
@DisplayName("AccountService unit tests")
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private AccountService accountService;

    private static final UUID TENANT_ID  = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(tenantContext.current()).thenReturn(TENANT_ID);
    }

    // ─── list ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list() without type returns all accounts")
    void list_noTypeFilter() {
        when(accountRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        accountService.list(null, PageRequest.of(0, 20));
        verify(accountRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
        verify(accountRepository, never()).findByTenantIdAndTypeAndDeletedAtIsNull(any(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("list() with type filter uses type query")
    void list_withTypeFilter() {
        when(accountRepository.findByTenantIdAndTypeAndDeletedAtIsNull(eq(TENANT_ID), eq("ASSET"), any()))
                .thenReturn(new PageImpl<>(List.of()));
        accountService.list("ASSET", PageRequest.of(0, 20));
        verify(accountRepository).findByTenantIdAndTypeAndDeletedAtIsNull(eq(TENANT_ID), eq("ASSET"), any());
    }

    // ─── getById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById() returns account when found")
    void getById_found() {
        Account acc = buildAccount(false);
        when(accountRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ACCOUNT_ID))
                .thenReturn(Optional.of(acc));
        Account result = accountService.getById(ACCOUNT_ID);
        assertThat(result.getId()).isEqualTo(ACCOUNT_ID);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND when missing")
    void getById_notFound() {
        when(accountRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ACCOUNT_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> accountService.getById(ACCOUNT_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── getByCode ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByCode() returns account for existing code")
    void getByCode_found() {
        Account acc = buildAccount(false);
        when(accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "1000"))
                .thenReturn(Optional.of(acc));
        Account result = accountService.getByCode("1000");
        assertThat(result.getCode()).isEqualTo("1000");
    }

    @Test
    @DisplayName("getByCode() throws NOT_FOUND for unknown code")
    void getByCode_notFound() {
        when(accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "XXXX"))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> accountService.getByCode("XXXX"));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() saves account with tenantId and default balance zero")
    void create_success() {
        Account req = buildAccount(false);
        req.setTenantId(null);
        req.setBalance(null);
        when(accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, req.getCode()))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Account result = accountService.create(req);

        assertThat(result.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(accountRepository).save(any());
    }

    @Test
    @DisplayName("create() throws CONFLICT when code already exists")
    void create_throwsOnDuplicateCode() {
        Account req = buildAccount(false);
        when(accountRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, req.getCode()))
                .thenReturn(Optional.of(req));
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> accountService.create(req));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
        verify(accountRepository, never()).save(any());
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update() saves updated fields")
    void update_success() {
        Account existing = buildAccount(false);
        when(accountRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ACCOUNT_ID))
                .thenReturn(Optional.of(existing));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Account body = new Account();
        body.setName("Updated Name");
        body.setType("LIABILITY");
        body.setSubType("PAYABLE");

        Account result = accountService.update(ACCOUNT_ID, body);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getType()).isEqualTo("LIABILITY");
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() soft-deletes a non-system account")
    void delete_softDeletes() {
        Account acc = buildAccount(false);
        when(accountRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ACCOUNT_ID))
                .thenReturn(Optional.of(acc));
        when(accountRepository.save(any())).thenReturn(acc);

        accountService.delete(ACCOUNT_ID);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("delete() throws FORBIDDEN for a system account")
    void delete_throwsForSystemAccount() {
        Account acc = buildAccount(true);
        when(accountRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ACCOUNT_ID))
                .thenReturn(Optional.of(acc));
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> accountService.delete(ACCOUNT_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        verify(accountRepository, never()).save(any());
    }

    // ─── adjustBalance ───────────────────────────────────────────────────────

    @Test
    @DisplayName("adjustBalance() adds positive amount to existing balance")
    void adjustBalance_increase() {
        Account acc = buildAccount(false);
        acc.setBalance(BigDecimal.valueOf(1000));
        when(accountRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ACCOUNT_ID))
                .thenReturn(Optional.of(acc));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        accountService.adjustBalance(ACCOUNT_ID, BigDecimal.valueOf(500));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }

    @Test
    @DisplayName("adjustBalance() subtracts negative amount from existing balance")
    void adjustBalance_decrease() {
        Account acc = buildAccount(false);
        acc.setBalance(BigDecimal.valueOf(1000));
        when(accountRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, ACCOUNT_ID))
                .thenReturn(Optional.of(acc));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        accountService.adjustBalance(ACCOUNT_ID, BigDecimal.valueOf(-200));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(800));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Account buildAccount(boolean system) {
        Account a = new Account();
        a.setId(ACCOUNT_ID);
        a.setTenantId(TENANT_ID);
        a.setCode("1000");
        a.setName("Cash");
        a.setType("ASSET");
        a.setSubType("CASH");
        a.setSystem(system);
        a.setBalance(BigDecimal.ZERO);
        a.setActive(true);
        return a;
    }
}

package com.erp.platform.config;

import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.AssetGroup;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.AssetGroupRepository;
import com.erp.platform.modules.inventory.entity.Warehouse;
import com.erp.platform.modules.inventory.repository.WarehouseRepository;
import com.erp.platform.modules.payroll.entity.SalaryComponent;
import com.erp.platform.modules.payroll.repository.SalaryComponentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MasterDataSeeder unit tests")
class MasterDataSeederTest {

    @Mock private AccountRepository accountRepo;
    @Mock private WarehouseRepository warehouseRepo;
    @Mock private SalaryComponentRepository salaryComponentRepo;
    @Mock private AssetGroupRepository assetGroupRepo;
    @InjectMocks private MasterDataSeeder masterDataSeeder;

    private static final UUID T = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // ─── GL accounts ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("seedMissingGlAccounts() creates account when subType is absent")
    void gl_createsWhenSubTypeMissing() throws Exception {
        when(accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(eq(T), anyString()))
                .thenReturn(List.of());
        when(accountRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        masterDataSeeder.run();

        verify(accountRepo, atLeast(18)).save(any());
    }

    @Test
    @DisplayName("seedMissingGlAccounts() skips accounts whose subType already exists")
    void gl_skipsExistingSubTypes() throws Exception {
        Account existing = new Account();
        existing.setId(UUID.randomUUID());
        when(accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(eq(T), anyString()))
                .thenReturn(List.of(existing));

        masterDataSeeder.run();

        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("seedMissingGlAccounts() sets correct type for ACCOUNTS_PAYABLE")
    void gl_accountsPayableHasLiabilityType() throws Exception {
        when(accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(eq(T), anyString()))
                .thenReturn(List.of());
        when(accountRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(warehouseRepo.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(T)).thenReturn(Optional.of(new Warehouse()));
        when(salaryComponentRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(T)).thenReturn(List.of(new SalaryComponent()));
        when(assetGroupRepo.findByTenantIdAndActiveAndDeletedAtIsNull(T, true)).thenReturn(List.of(new AssetGroup()));

        masterDataSeeder.run();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepo, atLeast(1)).save(captor.capture());
        assertThat(captor.getAllValues())
                .filteredOn(a -> "ACCOUNTS_PAYABLE".equals(a.getSubType()))
                .allMatch(a -> "LIABILITY".equals(a.getType()));
    }

    @Test
    @DisplayName("seedMissingGlAccounts() sets correct type for SALARY_EXPENSE")
    void gl_salaryExpenseHasExpenseType() throws Exception {
        when(accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(eq(T), anyString()))
                .thenReturn(List.of());
        when(accountRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(warehouseRepo.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(T)).thenReturn(Optional.of(new Warehouse()));
        when(salaryComponentRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(T)).thenReturn(List.of(new SalaryComponent()));
        when(assetGroupRepo.findByTenantIdAndActiveAndDeletedAtIsNull(T, true)).thenReturn(List.of(new AssetGroup()));

        masterDataSeeder.run();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepo, atLeast(1)).save(captor.capture());
        assertThat(captor.getAllValues())
                .filteredOn(a -> "SALARY_EXPENSE".equals(a.getSubType()))
                .allMatch(a -> "EXPENSE".equals(a.getType()));
    }

    // ─── Default Warehouse ────────────────────────────────────────────────────

    @Test
    @DisplayName("seedDefaultWarehouse() creates warehouse when none exists")
    void warehouse_createsWhenAbsent() throws Exception {
        when(accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(eq(T), anyString()))
                .thenReturn(List.of(new Account()));
        when(warehouseRepo.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(T))
                .thenReturn(Optional.empty());
        when(warehouseRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(salaryComponentRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(T))
                .thenReturn(List.of(new SalaryComponent()));
        when(assetGroupRepo.findByTenantIdAndActiveAndDeletedAtIsNull(T, true))
                .thenReturn(List.of(new AssetGroup()));

        masterDataSeeder.run();

        ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepo).save(captor.capture());
        assertThat(captor.getValue().isDefault()).isTrue();
        assertThat(captor.getValue().getCode()).isEqualTo("WH001");
    }

    @Test
    @DisplayName("seedDefaultWarehouse() skips when default warehouse already exists")
    void warehouse_skipsWhenExists() throws Exception {
        when(accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(eq(T), anyString()))
                .thenReturn(List.of(new Account()));
        when(warehouseRepo.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(T))
                .thenReturn(Optional.of(new Warehouse()));
        when(salaryComponentRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(T))
                .thenReturn(List.of(new SalaryComponent()));
        when(assetGroupRepo.findByTenantIdAndActiveAndDeletedAtIsNull(T, true))
                .thenReturn(List.of(new AssetGroup()));

        masterDataSeeder.run();

        verify(warehouseRepo, never()).save(any());
    }

    // ─── Salary Components ────────────────────────────────────────────────────

    @Test
    @DisplayName("seedSalaryComponents() creates 8 components when none exist")
    void salaryComponents_creates8WhenAbsent() throws Exception {
        when(accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(eq(T), anyString()))
                .thenReturn(List.of(new Account()));
        when(warehouseRepo.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(T))
                .thenReturn(Optional.of(new Warehouse()));
        when(salaryComponentRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(T))
                .thenReturn(List.of());
        when(salaryComponentRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(assetGroupRepo.findByTenantIdAndActiveAndDeletedAtIsNull(T, true))
                .thenReturn(List.of(new AssetGroup()));

        masterDataSeeder.run();

        verify(salaryComponentRepo, times(8)).save(any());
    }

    @Test
    @DisplayName("seedSalaryComponents() skips when components already exist")
    void salaryComponents_skipsWhenExist() throws Exception {
        when(accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(eq(T), anyString()))
                .thenReturn(List.of(new Account()));
        when(warehouseRepo.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(T))
                .thenReturn(Optional.of(new Warehouse()));
        when(salaryComponentRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(T))
                .thenReturn(List.of(new SalaryComponent()));
        when(assetGroupRepo.findByTenantIdAndActiveAndDeletedAtIsNull(T, true))
                .thenReturn(List.of(new AssetGroup()));

        masterDataSeeder.run();

        verify(salaryComponentRepo, never()).save(any());
    }

    // ─── Asset Groups ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("seedAssetGroups() creates 5 groups when none exist")
    void assetGroups_creates5WhenAbsent() throws Exception {
        when(accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(eq(T), anyString()))
                .thenReturn(List.of(new Account()));
        when(warehouseRepo.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(T))
                .thenReturn(Optional.of(new Warehouse()));
        when(salaryComponentRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(T))
                .thenReturn(List.of(new SalaryComponent()));
        when(assetGroupRepo.findByTenantIdAndActiveAndDeletedAtIsNull(T, true))
                .thenReturn(List.of());
        when(assetGroupRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        masterDataSeeder.run();

        verify(assetGroupRepo, times(5)).save(any());
    }

    @Test
    @DisplayName("seedAssetGroups() skips when groups already exist")
    void assetGroups_skipsWhenExist() throws Exception {
        when(accountRepo.findByTenantIdAndSubTypeAndDeletedAtIsNull(eq(T), anyString()))
                .thenReturn(List.of(new Account()));
        when(warehouseRepo.findByTenantIdAndIsDefaultTrueAndDeletedAtIsNull(T))
                .thenReturn(Optional.of(new Warehouse()));
        when(salaryComponentRepo.findByTenantIdAndActiveTrueAndDeletedAtIsNull(T))
                .thenReturn(List.of(new SalaryComponent()));
        when(assetGroupRepo.findByTenantIdAndActiveAndDeletedAtIsNull(T, true))
                .thenReturn(List.of(new AssetGroup()));

        masterDataSeeder.run();

        verify(assetGroupRepo, never()).save(any());
    }
}

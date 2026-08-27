package com.erp.platform.modules.crm;

import com.erp.platform.common.TestDataBuilder;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.crm.entity.Lead;
import com.erp.platform.modules.crm.entity.Lead.LeadPriority;
import com.erp.platform.modules.crm.entity.Lead.LeadStatus;
import com.erp.platform.modules.crm.repository.LeadRepository;
import com.erp.platform.modules.crm.service.LeadService;
import com.erp.platform.modules.master.repository.CustomerRepository;
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
@DisplayName("LeadService unit tests")
class LeadServiceTest {

    @Mock private LeadRepository leadRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private TenantContext tenantContext;
    @InjectMocks private LeadService leadService;

    private static final UUID TENANT_ID = TestDataBuilder.DEFAULT_TENANT_ID;
    private static final UUID LEAD_ID   = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(tenantContext.current()).thenReturn(TENANT_ID);
    }

    // ─── list ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list() without filter returns all leads")
    void list_noFilter() {
        when(leadRepository.findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any()))
                .thenReturn(new PageImpl<>(List.of()));
        leadService.list(null, null, PageRequest.of(0, 20));
        verify(leadRepository).findByTenantIdAndDeletedAtIsNull(eq(TENANT_ID), any());
    }

    @Test
    @DisplayName("list() with status filter uses status query")
    void list_withStatus() {
        when(leadRepository.findByTenantIdAndStatusAndDeletedAtIsNull(
                eq(TENANT_ID), eq(LeadStatus.NEW), any()))
                .thenReturn(new PageImpl<>(List.of()));
        leadService.list(LeadStatus.NEW, null, PageRequest.of(0, 20));
        verify(leadRepository).findByTenantIdAndStatusAndDeletedAtIsNull(
                eq(TENANT_ID), eq(LeadStatus.NEW), any());
    }

    @Test
    @DisplayName("list() with search term uses search query")
    void list_withSearch() {
        when(leadRepository.searchByTenantId(eq(TENANT_ID), eq("Acme"), any()))
                .thenReturn(new PageImpl<>(List.of()));
        leadService.list(null, "Acme", PageRequest.of(0, 20));
        verify(leadRepository).searchByTenantId(eq(TENANT_ID), eq("Acme"), any());
    }

    // ─── getById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById() returns lead when found")
    void getById_found() {
        Lead lead = buildLead(LeadStatus.NEW);
        when(leadRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, LEAD_ID))
                .thenReturn(Optional.of(lead));
        Lead result = leadService.getById(LEAD_ID);
        assertThat(result.getId()).isEqualTo(LEAD_ID);
    }

    @Test
    @DisplayName("getById() throws NOT_FOUND for missing lead")
    void getById_notFound() {
        when(leadRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, LEAD_ID))
                .thenReturn(Optional.empty());
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> leadService.getById(LEAD_ID));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() sets tenantId, NEW status and MEDIUM priority when absent")
    void create_setsDefaults() {
        Lead req = buildLead(null);
        req.setTenantId(null);
        req.setStatus(null);
        req.setPriority(null);
        when(leadRepository.save(any())).thenAnswer(i -> {
            Lead saved = i.getArgument(0);
            saved.setId(LEAD_ID);
            return saved;
        });

        Lead result = leadService.create(req);

        assertThat(result.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(result.getStatus()).isEqualTo(LeadStatus.NEW);
        assertThat(result.getPriority()).isEqualTo(LeadPriority.MEDIUM);
    }

    @Test
    @DisplayName("create() preserves status when explicitly provided")
    void create_preservesExplicitStatus() {
        Lead req = buildLead(LeadStatus.CONTACTED);
        when(leadRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Lead result = leadService.create(req);
        assertThat(result.getStatus()).isEqualTo(LeadStatus.CONTACTED);
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update() saves updated fields")
    void update_success() {
        Lead existing = buildLead(LeadStatus.NEW);
        when(leadRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, LEAD_ID))
                .thenReturn(Optional.of(existing));
        when(leadRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Lead body = buildLead(LeadStatus.CONTACTED);
        body.setName("Updated Name");
        body.setCompany("Updated Corp");

        Lead result = leadService.update(LEAD_ID, body);
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getStatus()).isEqualTo(LeadStatus.CONTACTED);
    }

    @Test
    @DisplayName("update() throws NOT_FOUND when lead missing")
    void update_notFound() {
        when(leadRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, LEAD_ID))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(AppException.class,
                () -> leadService.update(LEAD_ID, new Lead()));
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus() changes lead status")
    void updateStatus_changes() {
        Lead lead = buildLead(LeadStatus.CONTACTED);
        when(leadRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, LEAD_ID))
                .thenReturn(Optional.of(lead));
        when(leadRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Lead result = leadService.updateStatus(LEAD_ID, LeadStatus.QUALIFIED);
        assertThat(result.getStatus()).isEqualTo(LeadStatus.QUALIFIED);
    }

    @Test
    @DisplayName("updateStatus() auto-creates customer when lead is WON and email not duplicate")
    void updateStatus_wonAutoCreatesCustomer() {
        Lead lead = buildLead(LeadStatus.QUALIFIED);
        lead.setEmail("newcustomer@example.com");
        lead.setCompany("New Corp");
        when(leadRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, LEAD_ID))
                .thenReturn(Optional.of(lead));
        when(leadRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(customerRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(TENANT_ID, "newcustomer@example.com"))
                .thenReturn(false);
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Lead result = leadService.updateStatus(LEAD_ID, LeadStatus.WON);

        assertThat(result.getStatus()).isEqualTo(LeadStatus.WON);
        verify(customerRepository).save(any());
    }

    @Test
    @DisplayName("updateStatus() skips customer creation when email already exists")
    void updateStatus_wonSkipsCustomerWhenEmailExists() {
        Lead lead = buildLead(LeadStatus.QUALIFIED);
        lead.setEmail("existing@customer.com");
        when(leadRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, LEAD_ID))
                .thenReturn(Optional.of(lead));
        when(leadRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(customerRepository.existsByTenantIdAndEmailAndDeletedAtIsNull(TENANT_ID, "existing@customer.com"))
                .thenReturn(true);

        leadService.updateStatus(LEAD_ID, LeadStatus.WON);

        verify(customerRepository, never()).save(any());
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() soft-deletes lead")
    void delete_setsDeletedAt() {
        Lead lead = buildLead(LeadStatus.NEW);
        when(leadRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, LEAD_ID))
                .thenReturn(Optional.of(lead));
        when(leadRepository.save(any())).thenReturn(lead);

        leadService.delete(LEAD_ID);

        ArgumentCaptor<Lead> captor = ArgumentCaptor.forClass(Lead.class);
        verify(leadRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("delete() throws NOT_FOUND for missing lead")
    void delete_notFound() {
        when(leadRepository.findByTenantIdAndIdAndDeletedAtIsNull(TENANT_ID, LEAD_ID))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(AppException.class, () -> leadService.delete(LEAD_ID));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Lead buildLead(LeadStatus status) {
        Lead l = new Lead();
        l.setId(LEAD_ID);
        l.setTenantId(TENANT_ID);
        l.setName("John Prospect");
        l.setCompany("Prospect Corp");
        l.setEmail("john@prospect.com");
        l.setStatus(status != null ? status : LeadStatus.NEW);
        l.setPriority(LeadPriority.MEDIUM);
        return l;
    }
}

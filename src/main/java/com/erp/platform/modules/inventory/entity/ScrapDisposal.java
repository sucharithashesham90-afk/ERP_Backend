package com.erp.platform.modules.inventory.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "scrap_disposals",
       indexes = {@Index(name = "idx_scrap_disp_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class ScrapDisposal extends TenantEntity {

    public enum DisposalMethod {
        SOLD, RECYCLED, DESTROYED, RETURNED_TO_VENDOR, DONATED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrap_entry_id", nullable = false)
    private ScrapEntry scrapEntry;

    @Column(name = "disposal_date")
    private LocalDate disposalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposal_method", length = 30)
    private DisposalMethod disposalMethod;

    @Column(name = "disposed_to", length = 200)
    private String disposedTo;

    @Column(name = "disposal_amount", precision = 18, scale = 2)
    private BigDecimal disposalAmount = BigDecimal.ZERO;

    @Column(name = "transport_cost", precision = 18, scale = 2)
    private BigDecimal transportCost = BigDecimal.ZERO;

    @Column(name = "net_recovery", precision = 18, scale = 2)
    private BigDecimal netRecovery;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(length = 1000)
    private String notes;
}

package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "period_closes",
       indexes = {@Index(name = "idx_period_close_tenant", columnList = "tenant_id"),
                  @Index(name = "idx_period_close_year_month", columnList = "tenant_id, period_year, period_month")})
@Getter
@Setter
public class PeriodClose extends TenantEntity {

    @Column(name = "period_year", nullable = false)
    private int periodYear;

    @Column(name = "period_month", nullable = false)
    private int periodMonth;

    @Column(name = "period_name", length = 50)
    private String periodName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CloseStatus status = CloseStatus.OPEN;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by", length = 150)
    private String closedBy;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "frozen_at")
    private LocalDateTime frozenAt;

    @Column(name = "frozen_by", length = 150)
    private String frozenBy;

    @Column(name = "opening_balance", precision = 18, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance", precision = 18, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "total_debits", precision = 18, scale = 2)
    private BigDecimal totalDebits;

    @Column(name = "total_credits", precision = 18, scale = 2)
    private BigDecimal totalCredits;

    @Column(length = 1000)
    private String notes;

    public enum CloseStatus {
        OPEN, CLOSING_IN_PROGRESS, CLOSED, REOPENED, FROZEN
    }
}

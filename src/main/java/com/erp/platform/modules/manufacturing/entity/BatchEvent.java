package com.erp.platform.modules.manufacturing.entity;

import com.erp.platform.common.entity.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch_events",
       indexes = {
           @Index(name = "idx_bevt_tenant", columnList = "tenant_id"),
           @Index(name = "idx_bevt_batch", columnList = "batch_id")
       })
@Getter
@Setter
public class BatchEvent extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BatchHistory batch;

    @Column(name = "event_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(length = 1000)
    private String description;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(length = 500)
    private String result;

    public enum EventType {
        CREATED, QUALITY_TEST, STOCK_MOVEMENT, DISPATCHED, ADJUSTED, STATUS_CHANGED
    }
}

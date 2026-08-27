package com.erp.platform.modules.intake.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Generic intake document (lot-wise / sales-return / third-party). The full submitted
 * form — including nested line/test arrays — is persisted as JSON in {@code payloadJson}
 * so it round-trips exactly; a few columns are promoted for listing/sorting.
 */
@Entity
@Table(name = "intake_documents",
       indexes = {@Index(name = "idx_intakedoc_tenant_type", columnList = "tenant_id,type")})
@Getter
@Setter
public class IntakeDocument extends TenantEntity {

    /** LOT_WISE | SALES_RETURN | THIRD_PARTY */
    @Column(nullable = false, length = 30)
    private String type;

    @Column(name = "intake_slip", length = 80)
    private String intakeSlip;

    @Column(name = "intake_date")
    private LocalDate intakeDate;

    @Column(name = "payload_json", columnDefinition = "text")
    private String payloadJson;
}

package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "samples",
       indexes = {
           @Index(name = "idx_sample_tenant", columnList = "tenant_id"),
           @Index(name = "idx_sample_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class Sample extends TenantEntity {

    @Column(name = "sample_number", nullable = false, length = 50)
    private String sampleNumber;

    @Column(name = "material_group_id")
    private UUID materialGroupId;

    @Column(name = "material_group_name", length = 200)
    private String materialGroupName;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "material_item_id")
    private UUID materialItemId;

    @Column(name = "material_item_name", length = 200)
    private String materialItemName;

    @Column(name = "source_type", length = 20)
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "source_reference", length = 100)
    private String sourceReference;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "sample_date")
    private LocalDate sampleDate;

    @Column(name = "sample_size", precision = 18, scale = 4)
    private BigDecimal sampleSize;

    @Column(length = 50)
    private String unit;

    @Column(name = "collected_by", length = 100)
    private String collectedBy;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private SampleStatus status = SampleStatus.COLLECTED;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    // ── Seed classification (Sample Submission is driven by crop group / crop / variety / seed state / location) ──
    @Column(name = "crop_group_id") private String cropGroupId;
    @Column(name = "crop_group_name", length = 200) private String cropGroupName;
    @Column(name = "crop_id") private String cropId;
    @Column(name = "crop_name", length = 200) private String cropName;
    @Column(name = "variety_id") private String varietyId;
    @Column(name = "variety_name", length = 200) private String varietyName;
    @Column(name = "seed_state_id") private String seedStateId;
    @Column(name = "seed_state_name", length = 200) private String seedStateName;
    @Column(name = "sample_location", length = 200) private String location;

    /** Links the sample to a Crop/Variety Test config — drives sample quantity, inventory deduction and property standards. */
    @Column(name = "crop_variety_test_id")
    private UUID cropVarietyTestId;

    /** Shared batch number assigned when multiple samples are batched together for testing. */
    @Column(name = "batch_number", length = 40)
    private String batchNumber;

    /** Result Entry outcome — PASS / FAIL / PENDING. */
    @Column(name = "result_status", length = 20)
    private String resultStatus;

    /** JSON of observed property values captured at Result Entry. */
    @Column(name = "results_json", columnDefinition = "TEXT")
    private String resultsJson;

    @Column(length = 1000)
    private String notes;

    public enum SourceType {
        INTAKE, PRODUCTION, STOCK, SUPPLIER
    }

    public enum SampleStatus {
        COLLECTED, SUBMITTED, UNDER_TEST, COMPLETED, DISPOSED
    }
}

package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "AgriPublishResult")
@Table(name = "agri_publish_results", indexes = {@Index(name = "idx_pr_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class PublishResult extends TenantEntity {

    @Column(name = "publish_number", length = 50, nullable = false)
    private String publishNumber;

    /** The exact Sample this row was published from — set only for rows created via the bulk
     *  publish action on Result Entry; older/manual rows may have this null. */
    @Column(name = "sample_id")
    private UUID sampleId;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "lot_number", length = 100)
    private String lotNumber;

    @Column(name = "test_result_number", length = 50)
    private String testResultNumber;

    @Column(name = "published_by", length = 100)
    private String publishedBy;

    @Column(name = "publication_note", length = 500)
    private String publicationNote;

    @Column(name = "publish_status", length = 20)
    private String publishStatus = "DRAFT";
}

package com.erp.platform.modules.promotions.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * An omni-channel marketing campaign (Email / SMS / WhatsApp) composed for a set of recipients
 * (customers, growers, organizers, employees). Recipients are stored as JSON so the campaign is a
 * self-contained audit of who it was sent to.
 */
@Entity
@Table(name = "marketing_campaigns", indexes = {@Index(name = "idx_marketing_campaign_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class MarketingCampaign extends TenantEntity {

    @Column(name = "campaign_name", length = 200, nullable = false)
    private String campaignName;

    @Column(name = "template_name", length = 200)
    private String templateName;

    /** Channels selected, CSV of EMAIL,SMS,WHATSAPP. */
    @Column(name = "channels", length = 60)
    private String channels = "EMAIL";

    @Column(name = "subject", length = 300)
    private String subject;

    @Column(name = "body", columnDefinition = "text")
    private String body;

    /** Target groups selected, CSV of CUSTOMER,GROWER,ORGANIZER,EMPLOYEE. */
    @Column(name = "target_groups", length = 120)
    private String targetGroups;

    /** Recipients as JSON: [{id,name,email,phone,type}]. */
    @Column(name = "recipients_json", columnDefinition = "text")
    private String recipientsJson;

    @Column(name = "recipient_count")
    private Integer recipientCount = 0;

    @Column(name = "sent_count")
    private Integer sentCount = 0;

    /** DRAFT | SENT */
    @Column(name = "status", length = 20)
    private String status = "DRAFT";

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}

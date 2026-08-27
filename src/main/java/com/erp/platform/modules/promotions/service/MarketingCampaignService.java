package com.erp.platform.modules.promotions.service;

import com.erp.platform.common.email.EmailService;
import com.erp.platform.common.sms.SmsService;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.promotions.entity.MarketingCampaign;
import com.erp.platform.modules.promotions.repository.MarketingCampaignRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MarketingCampaignService {

    private final MarketingCampaignRepository repository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final TenantContext tenantContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Page<MarketingCampaign> list(Pageable pageable) {
        return repository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable);
    }

    public MarketingCampaign getOrThrow(UUID id) {
        return repository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Campaign not found: " + id));
    }

    @Transactional
    public MarketingCampaign create(Map<String, Object> req) {
        MarketingCampaign c = new MarketingCampaign();
        c.setTenantId(tenantContext.current());
        apply(c, req);
        return repository.save(c);
    }

    @Transactional
    public MarketingCampaign update(UUID id, Map<String, Object> req) {
        MarketingCampaign c = getOrThrow(id);
        apply(c, req);
        return repository.save(c);
    }

    @Transactional
    public void delete(UUID id) {
        MarketingCampaign c = getOrThrow(id);
        c.setDeletedAt(LocalDateTime.now());
        repository.save(c);
    }

    private void apply(MarketingCampaign c, Map<String, Object> req) {
        if (req.get("campaignName") != null) c.setCampaignName(str(req, "campaignName"));
        if (c.getCampaignName() == null || c.getCampaignName().isBlank())
            throw AppException.badRequest("Campaign name is required");
        c.setTemplateName(str(req, "templateName"));
        c.setChannels(str(req, "channels"));
        c.setSubject(str(req, "subject"));
        c.setBody(str(req, "body"));
        c.setTargetGroups(str(req, "targetGroups"));
        Object recipients = req.get("recipients");
        if (recipients != null) {
            try { c.setRecipientsJson(objectMapper.writeValueAsString(recipients)); } catch (Exception ignore) {}
            if (recipients instanceof List<?> l) c.setRecipientCount(l.size());
        } else if (req.get("recipientsJson") != null) {
            c.setRecipientsJson(str(req, "recipientsJson"));
        }
    }

    /** Send a single test message to a test email using the campaign's subject/body. */
    @Transactional(readOnly = true)
    public boolean sendTest(UUID id, String testEmail) {
        MarketingCampaign c = getOrThrow(id);
        if (testEmail == null || testEmail.isBlank()) throw AppException.badRequest("A test email is required");
        String html = personalize(c.getBody(), "there");
        return emailService.sendHtml(testEmail, "[TEST] " + nz(c.getSubject()), html);
    }

    /** Launch the campaign: email everyone with an email; SMS/WhatsApp simulated for those with a phone. */
    @Transactional
    public Map<String, Object> launch(UUID id) {
        MarketingCampaign c = getOrThrow(id);
        List<Map<String, Object>> recipients = parseRecipients(c.getRecipientsJson());
        if (recipients.isEmpty()) throw AppException.badRequest("No recipients selected for this campaign");

        String channels = nz(c.getChannels()).toUpperCase();
        boolean email = channels.isEmpty() || channels.contains("EMAIL");
        boolean whatsApp = channels.contains("WHATSAPP");
        boolean sms = channels.contains("SMS") && !whatsApp; // prefer WhatsApp if both selected

        int emailsSent = 0, messagesSent = 0;
        for (Map<String, Object> r : recipients) {
            String name = firstName(str(r, "name"));
            String to = str(r, "email");
            String phone = str(r, "phone");
            String msg = personalize(nz(c.getBody()), name);
            if (email && to != null && !to.isBlank()) {
                if (emailService.sendHtml(to, personalize(nz(c.getSubject()), name), msg)) emailsSent++;
            }
            if (phone != null && !phone.isBlank()) {
                boolean ok = whatsApp ? smsService.sendWhatsApp(phone, msg)
                        : sms ? smsService.sendSms(phone, msg) : false;
                if (ok) messagesSent++;
            }
        }
        int total = emailsSent + messagesSent;
        c.setSentCount(total);
        c.setStatus("SENT");
        c.setSentAt(LocalDateTime.now());
        repository.save(c);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("campaignId", c.getId());
        out.put("recipientCount", recipients.size());
        out.put("emailsSent", emailsSent);
        out.put("messagesSent", messagesSent);
        out.put("totalSent", total);
        out.put("status", c.getStatus());
        return out;
    }

    private List<Map<String, Object>> parseRecipients(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.warn("Could not parse recipientsJson: {}", e.getMessage());
            return List.of();
        }
    }

    private static String personalize(String template, String firstName) {
        if (template == null) return "";
        return template.replace("{first_name}", firstName == null ? "" : firstName);
    }

    private static String firstName(String name) {
        if (name == null || name.isBlank()) return "there";
        return name.trim().split("\\s+")[0];
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? null : v.toString();
    }
    private static String nz(String s) { return s == null ? "" : s; }
    private static String truncate(String s) { return s == null ? "" : (s.length() > 80 ? s.substring(0, 80) + "…" : s); }
}

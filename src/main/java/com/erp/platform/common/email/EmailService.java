package com.erp.platform.common.email;

import com.erp.platform.modules.master.entity.Customer;
import com.erp.platform.modules.master.entity.Vendor;
import com.erp.platform.modules.purchase.entity.PurchaseOrder;
import com.erp.platform.modules.purchase.entity.PurchaseOrderItem;
import com.erp.platform.modules.sales.entity.Quotation;
import com.erp.platform.modules.sales.entity.QuotationItem;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@erp.local}")
    private String fromAddress;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    /**
     * Generic HTML email send (used by marketing campaigns). When mail is disabled (dev) it logs and
     * reports success so campaign flows can be exercised without a live SMTP server.
     */
    public boolean sendHtml(String to, String subject, String htmlBody) {
        if (to == null || to.isBlank()) return false;
        if (!mailEnabled) {
            log.info("[MAIL DISABLED] Would send to {} — subject: {}", to, subject);
            return true;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject != null ? subject : "");
            helper.setText(htmlBody != null ? htmlBody : "", true);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.warn("Email send failed to {}: {}", to, e.getMessage());
            return false;
        }
    }

    public boolean sendPurchaseOrderToVendor(PurchaseOrder po, Vendor vendor) {
        if (!mailEnabled) {
            log.info("Mail disabled — skipping PO email for {}", po.getPoNumber());
            return false;
        }
        String vendorEmail = vendor.getEmail();
        if (vendorEmail == null || vendorEmail.isBlank()) {
            log.warn("Vendor {} has no email — skipping PO email for {}", vendor.getName(), po.getPoNumber());
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(vendorEmail);
            helper.setSubject("Purchase Order " + po.getPoNumber());
            helper.setText(buildPoHtml(po, vendor), true);
            mailSender.send(message);
            log.info("PO email sent to {} for {}", vendorEmail, po.getPoNumber());
            return true;
        } catch (Exception e) {
            log.error("Failed to send PO email for {}: {}", po.getPoNumber(), e.getMessage());
            return false;
        }
    }

    private String buildPoHtml(PurchaseOrder po, Vendor vendor) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.ENGLISH);
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);

        StringBuilder sb = new StringBuilder();
        sb.append("""
                <html><head><style>
                  body { font-family: Arial, sans-serif; color: #333; font-size: 14px; }
                  h2 { color: #1976d2; }
                  table { width: 100%; border-collapse: collapse; margin-top: 16px; }
                  th { background: #1976d2; color: #fff; padding: 8px; text-align: left; }
                  td { border: 1px solid #ddd; padding: 8px; }
                  tr:nth-child(even) td { background: #f9f9f9; }
                  .total-row td { font-weight: bold; background: #e3f2fd; }
                  .footer { margin-top: 24px; color: #666; font-size: 12px; }
                  .info-grid { display: flex; gap: 40px; margin: 16px 0; }
                  .info-block p { margin: 4px 0; }
                  .label { color: #666; font-size: 12px; }
                </style></head><body>
                """);

        sb.append("<h2>Purchase Order</h2>");
        sb.append("<p>Dear <strong>").append(escape(vendor.getContactPerson() != null ? vendor.getContactPerson() : vendor.getName())).append("</strong>,</p>");
        sb.append("<p>Please find below the details of our Purchase Order. Kindly confirm receipt and expected delivery.</p>");

        sb.append("<div class='info-grid'>");
        sb.append("<div class='info-block'>");
        appendInfo(sb, "PO Number", po.getPoNumber());
        appendInfo(sb, "PO Date", po.getOrderDate() != null ? po.getOrderDate().toString() : "—");
        appendInfo(sb, "Expected Delivery", po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().toString() : "—");
        if (po.getQuotationReference() != null) appendInfo(sb, "Your Quotation Ref", po.getQuotationReference());
        sb.append("</div><div class='info-block'>");
        appendInfo(sb, "Vendor", vendor.getName());
        if (po.getDeliveryAddress() != null) appendInfo(sb, "Delivery Address", po.getDeliveryAddress());
        if (po.getPaymentTerms() != null) appendInfo(sb, "Payment Terms", po.getPaymentTerms());
        sb.append("</div></div>");

        if (po.getSubject() != null) {
            sb.append("<p><strong>Subject:</strong> ").append(escape(po.getSubject())).append("</p>");
        }

        sb.append("<table><thead><tr>")
          .append("<th>#</th><th>Product / Description</th><th>Qty</th><th>Unit</th>")
          .append("<th>Unit Price (₹)</th><th>Discount</th><th>Tax %</th><th>Total (₹)</th>")
          .append("</tr></thead><tbody>");

        List<PurchaseOrderItem> items = po.getItems();
        if (items != null) {
            int i = 1;
            for (PurchaseOrderItem item : items) {
                sb.append("<tr>")
                  .append("<td>").append(i++).append("</td>")
                  .append("<td>").append(escape(item.getProductName())).append("</td>")
                  .append("<td align='right'>").append(fmt.format(item.getQuantity())).append("</td>")
                  .append("<td>").append(item.getUnit() != null ? escape(item.getUnit()) : "").append("</td>")
                  .append("<td align='right'>").append(fmt.format(item.getUnitPrice())).append("</td>")
                  .append("<td align='right'>")
                    .append(item.getDiscountPercent() != null && item.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0
                        ? fmt.format(item.getDiscountPercent()) + "%" : "—")
                  .append("</td>")
                  .append("<td align='right'>").append(fmt.format(item.getTaxPercent())).append("%</td>")
                  .append("<td align='right'>").append(fmt.format(item.getTotalAmount())).append("</td>")
                  .append("</tr>");
            }
        }
        sb.append("</tbody></table>");

        sb.append("<table style='width:320px;margin-top:12px;margin-left:auto'><tbody>");
        if (po.getSubtotal() != null)
            sb.append("<tr><td>Subtotal</td><td align='right'>₹ ").append(fmt.format(po.getSubtotal())).append("</td></tr>");
        if (po.getTaxAmount() != null)
            sb.append("<tr><td>Tax</td><td align='right'>₹ ").append(fmt.format(po.getTaxAmount())).append("</td></tr>");
        if (po.getDiscountAmount() != null && po.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0)
            sb.append("<tr><td>Header Discount</td><td align='right'>− ₹ ").append(fmt.format(po.getDiscountAmount())).append("</td></tr>");
        if (po.getFreightCharges() != null && po.getFreightCharges().compareTo(BigDecimal.ZERO) > 0)
            sb.append("<tr><td>Freight</td><td align='right'>₹ ").append(fmt.format(po.getFreightCharges())).append("</td></tr>");
        if (po.getTotalAmount() != null)
            sb.append("<tr class='total-row'><td><strong>Net Total</strong></td><td align='right'><strong>₹ ").append(fmt.format(po.getTotalAmount())).append("</strong></td></tr>");
        sb.append("</tbody></table>");

        if (po.getDeliveryTerms() != null) {
            sb.append("<p style='margin-top:16px'><strong>Delivery Terms:</strong> ").append(escape(po.getDeliveryTerms())).append("</p>");
        }
        if (po.getQualityTerms() != null) {
            sb.append("<p><strong>Quality Terms:</strong> ").append(escape(po.getQualityTerms())).append("</p>");
        }
        if (po.getNotes() != null && !po.getNotes().isBlank()) {
            sb.append("<p><strong>Notes:</strong> ").append(escape(po.getNotes())).append("</p>");
        }

        sb.append("<div class='footer'><p>This is a system-generated Purchase Order. Please do not reply to this email.</p></div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    public boolean sendQuotationToCustomer(Quotation q, Customer customer) {
        if (!mailEnabled) {
            log.info("Mail disabled — skipping quotation email for {}", q.getQuotationNumber());
            return false;
        }
        String customerEmail = customer.getEmail();
        if (customerEmail == null || customerEmail.isBlank()) {
            log.warn("Customer {} has no email — skipping quotation email for {}", customer.getName(), q.getQuotationNumber());
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(customerEmail);
            helper.setSubject("Quotation " + q.getQuotationNumber());
            helper.setText(buildQuotationHtml(q, customer), true);
            mailSender.send(message);
            log.info("Quotation email sent to {} for {}", customerEmail, q.getQuotationNumber());
            return true;
        } catch (Exception e) {
            log.error("Failed to send quotation email for {}: {}", q.getQuotationNumber(), e.getMessage());
            return false;
        }
    }

    private String buildQuotationHtml(Quotation q, Customer customer) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.ENGLISH);
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);

        StringBuilder sb = new StringBuilder();
        sb.append("""
                <html><head><style>
                  body { font-family: Arial, sans-serif; color: #333; font-size: 14px; }
                  h2 { color: #2e7d32; }
                  table { width: 100%; border-collapse: collapse; margin-top: 16px; }
                  th { background: #2e7d32; color: #fff; padding: 8px; text-align: left; }
                  td { border: 1px solid #ddd; padding: 8px; }
                  tr:nth-child(even) td { background: #f9f9f9; }
                  .total-row td { font-weight: bold; background: #e8f5e9; }
                  .footer { margin-top: 24px; color: #666; font-size: 12px; }
                  .info-grid { display: flex; gap: 40px; margin: 16px 0; }
                  .info-block p { margin: 4px 0; }
                  .label { color: #666; font-size: 12px; }
                </style></head><body>
                """);

        sb.append("<h2>Quotation</h2>");
        sb.append("<p>Dear <strong>").append(escape(customer.getName())).append("</strong>,</p>");
        sb.append("<p>Please find below our quotation for your reference. We look forward to your order.</p>");

        sb.append("<div class='info-grid'>");
        sb.append("<div class='info-block'>");
        appendInfo(sb, "Quotation Number", q.getQuotationNumber());
        appendInfo(sb, "Date", q.getQuotationDate() != null ? q.getQuotationDate().toString() : "—");
        appendInfo(sb, "Valid Until", q.getValidUntil() != null ? q.getValidUntil().toString() : "—");
        sb.append("</div><div class='info-block'>");
        appendInfo(sb, "Customer", customer.getName());
        if (q.getTerms() != null) appendInfo(sb, "Payment Terms", q.getTerms());
        sb.append("</div></div>");

        sb.append("<table><thead><tr>")
          .append("<th>#</th><th>Product / Description</th><th>Qty</th><th>Unit</th>")
          .append("<th>Unit Price (₹)</th><th>Discount</th><th>Tax %</th><th>Total (₹)</th>")
          .append("</tr></thead><tbody>");

        List<QuotationItem> items = q.getItems();
        if (items != null) {
            int i = 1;
            for (QuotationItem item : items) {
                sb.append("<tr>")
                  .append("<td>").append(i++).append("</td>")
                  .append("<td>").append(escape(item.getProductName()))
                    .append(item.getDescription() != null ? "<br><small style='color:#666'>" + escape(item.getDescription()) + "</small>" : "")
                  .append("</td>")
                  .append("<td align='right'>").append(fmt.format(item.getQuantity())).append("</td>")
                  .append("<td>").append(item.getUnit() != null ? escape(item.getUnit()) : "").append("</td>")
                  .append("<td align='right'>").append(fmt.format(item.getUnitPrice())).append("</td>")
                  .append("<td align='right'>")
                    .append(item.getDiscountPercent() != null && item.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0
                        ? fmt.format(item.getDiscountPercent()) + "%" : "—")
                  .append("</td>")
                  .append("<td align='right'>").append(fmt.format(item.getTaxPercent())).append("%</td>")
                  .append("<td align='right'>").append(fmt.format(item.getTotalAmount())).append("</td>")
                  .append("</tr>");
            }
        }
        sb.append("</tbody></table>");

        sb.append("<table style='width:320px;margin-top:12px;margin-left:auto'><tbody>");
        if (q.getSubtotal() != null)
            sb.append("<tr><td>Subtotal</td><td align='right'>₹ ").append(fmt.format(q.getSubtotal())).append("</td></tr>");
        if (q.getTaxAmount() != null)
            sb.append("<tr><td>Tax</td><td align='right'>₹ ").append(fmt.format(q.getTaxAmount())).append("</td></tr>");
        if (q.getDiscountAmount() != null && q.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0)
            sb.append("<tr><td>Discount</td><td align='right'>− ₹ ").append(fmt.format(q.getDiscountAmount())).append("</td></tr>");
        if (q.getTotalAmount() != null)
            sb.append("<tr class='total-row'><td><strong>Net Total</strong></td><td align='right'><strong>₹ ").append(fmt.format(q.getTotalAmount())).append("</strong></td></tr>");
        sb.append("</tbody></table>");

        if (q.getNotes() != null && !q.getNotes().isBlank()) {
            sb.append("<p style='margin-top:16px'><strong>Notes:</strong> ").append(escape(q.getNotes())).append("</p>");
        }
        sb.append("<div class='footer'><p>This is a system-generated Quotation. To place an order, please contact us.</p></div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private void appendInfo(StringBuilder sb, String label, String value) {
        sb.append("<p><span class='label'>").append(escape(label)).append("</span><br><strong>").append(escape(value)).append("</strong></p>");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}

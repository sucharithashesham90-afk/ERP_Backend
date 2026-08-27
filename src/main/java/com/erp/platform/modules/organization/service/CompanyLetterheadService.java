package com.erp.platform.modules.organization.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.organization.entity.Company;
import com.erp.platform.modules.organization.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The company block that heads every document this system prints.
 *
 * <p>Documents were each inventing their own heading, or going without: the voucher print carried
 * the name of the software, the payment advice carried the literal word "Company", and the tax
 * invoice carried the customer's address but never the seller's — which a tax invoice is required
 * to show. A document that does not say who issued it cannot be filed, sent, or relied on.
 *
 * <p>Name large, address and contact details small beneath it. Returns an empty string when no
 * company is configured: a blank heading is honest, a placeholder is not.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyLetterheadService {

    private final CompanyRepository companyRepository;
    private final TenantContext tenantContext;

    /** CSS for {@link #html()}. Include once in the document's &lt;style&gt; block. */
    public static final String CSS =
            ".co{border-bottom:2px solid #C08A4E;padding-bottom:8px;margin-bottom:14px}" +
            ".co .nm{font-size:23px;font-weight:800;letter-spacing:0.3px;line-height:1.15;color:#2E2016}" +
            ".co .ln{font-size:10px;color:#555;margin-top:2px;line-height:1.35}";

    /** The letterhead as an HTML block, or "" when there is no company to show. */
    public String html() {
        Company c = current();
        if (c == null) return "";

        String name = firstNonBlank(c.getLegalName(), c.getName());
        if (name == null) return "";

        String place = join(", ", c.getAddress(), c.getCity(), c.getState(), c.getPostalCode(), c.getCountry());
        String contact = join(" · ",
                blank(c.getPhone()) ? null : "Ph: " + c.getPhone(),
                c.getEmail(), c.getWebsite());
        String tax = join(" · ",
                blank(c.getGstin()) ? null : "GSTIN: " + c.getGstin(),
                blank(c.getTaxNumber()) ? null : "Tax No: " + c.getTaxNumber(),
                blank(c.getCin()) ? null : "CIN: " + c.getCin());

        StringBuilder sb = new StringBuilder("<div class='co'><div class='nm'>").append(esc(name)).append("</div>");
        if (!place.isBlank())   sb.append("<div class='ln'>").append(esc(place)).append("</div>");
        if (!contact.isBlank()) sb.append("<div class='ln'>").append(esc(contact)).append("</div>");
        if (!tax.isBlank())     sb.append("<div class='ln'>").append(esc(tax)).append("</div>");
        return sb.append("</div>").toString();
    }

    private Company current() {
        try {
            var page = companyRepository.findByTenantIdAndDeletedAtIsNull(
                    tenantContext.current(), PageRequest.of(0, 1));
            return page.isEmpty() ? null : page.getContent().get(0);
        } catch (Exception e) {
            // A missing letterhead must never stop a document printing.
            log.warn("Could not load company for letterhead: {}", e.getMessage());
            return null;
        }
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }

    private static String firstNonBlank(String... vals) {
        return Stream.of(vals).filter(v -> !blank(v)).findFirst().orElse(null);
    }

    private static String join(String sep, String... parts) {
        return Stream.of(parts).filter(p -> !blank(p)).collect(Collectors.joining(sep));
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

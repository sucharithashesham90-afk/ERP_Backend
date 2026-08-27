package com.erp.platform.modules.accounting.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.accounting.dto.BankVoucherDto;
import com.erp.platform.modules.accounting.dto.BankVoucherLineDto;
import com.erp.platform.modules.accounting.dto.CreateBankVoucherRequest;
import com.erp.platform.modules.accounting.entity.Account;
import com.erp.platform.modules.accounting.entity.BankVoucher;
import com.erp.platform.modules.accounting.entity.BankVoucher.VoucherType;
import com.erp.platform.modules.accounting.entity.BankVoucherLine;
import com.erp.platform.modules.accounting.entity.JournalEntry;
import com.erp.platform.modules.accounting.entity.JournalEntryLine;
import com.erp.platform.modules.accounting.entity.BankAccount;
import com.erp.platform.modules.accounting.repository.AccountRepository;
import com.erp.platform.modules.accounting.repository.BankAccountRepository;
import com.erp.platform.modules.accounting.repository.BankVoucherRepository;
import com.erp.platform.modules.accounting.repository.JournalEntryRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BankVoucherService {

    private final BankVoucherRepository bankVoucherRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryService journalEntryService;
    private final AccountRepository accountRepository;
    private final BankAccountRepository bankAccountRepository;
    /** Printed documents carry the company's own letterhead, not the word "Company". */
    private final com.erp.platform.modules.organization.service.CompanyLetterheadService letterhead;
    private final TenantContext tenantContext;

    public PageResponse<BankVoucherDto> list(VoucherType type, String status,
            LocalDate dateFrom, LocalDate dateTo, String paymentMode, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        Specification<BankVoucher> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (type != null)
                predicates.add(cb.equal(root.get("voucherType"), type));
            if (status != null && !status.isBlank())
                predicates.add(cb.equal(root.get("status"), status));
            if (dateFrom != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("voucherDate"), dateFrom));
            if (dateTo != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("voucherDate"), dateTo));
            if (paymentMode != null && !paymentMode.isBlank())
                predicates.add(cb.equal(root.get("paymentMode"), paymentMode));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(bankVoucherRepository.findAll(spec, pageable).map(this::toDto));
    }

    public BankVoucherDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public BankVoucherDto create(CreateBankVoucherRequest request) {
        UUID tenantId = tenantContext.current();
        BankVoucher voucher = new BankVoucher();
        voucher.setTenantId(tenantId);
        voucher.setVoucherNumber(generateNumber(request.getVoucherType()));
        voucher.setStatus("DRAFT");
        mapFromRequest(voucher, request, tenantId);
        BankVoucher saved = bankVoucherRepository.save(voucher);
        if (request.isPostImmediately()) {
            postVoucher(saved);
        }
        log.info("BankVoucher created: id={}, number={}", saved.getId(), saved.getVoucherNumber());
        return toDto(saved);
    }

    @Transactional
    public BankVoucherDto update(UUID id, CreateBankVoucherRequest request) {
        BankVoucher voucher = findOrThrow(id);
        if (!"DRAFT".equals(voucher.getStatus())) {
            throw AppException.badRequest("Only DRAFT vouchers can be edited");
        }
        mapFromRequest(voucher, request, voucher.getTenantId());
        return toDto(bankVoucherRepository.save(voucher));
    }

    @Transactional
    public BankVoucherDto post(UUID id) {
        BankVoucher voucher = findOrThrow(id);
        if (!"DRAFT".equals(voucher.getStatus())) {
            throw AppException.badRequest("Only DRAFT vouchers can be posted");
        }
        postVoucher(voucher);
        return toDto(voucher);
    }

    @Transactional
    public BankVoucherDto cancel(UUID id) {
        BankVoucher voucher = findOrThrow(id);
        if ("CANCELLED".equals(voucher.getStatus())) {
            throw AppException.badRequest("Voucher is already cancelled");
        }
        voucher.setStatus("CANCELLED");
        return toDto(bankVoucherRepository.save(voucher));
    }

    @Transactional
    public void delete(UUID id) {
        BankVoucher voucher = findOrThrow(id);
        if (!"DRAFT".equals(voucher.getStatus())) {
            throw AppException.badRequest("Only DRAFT vouchers can be deleted");
        }
        voucher.setDeletedAt(LocalDateTime.now());
        bankVoucherRepository.save(voucher);
    }

    public List<Map<String, Object>> getOutstandingEntries(UUID accountId) {
        UUID tenantId = tenantContext.current();
        var pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "entryDate"));
        return journalEntryRepository
                .findByTenantIdAndLineAccountId(tenantId, accountId, pageable)
                .getContent()
                .stream()
                .map(je -> {
                    BigDecimal amount = je.getLines().stream()
                            .filter(l -> accountId.equals(l.getAccountId()))
                            .map(l -> {
                                BigDecimal dr = l.getDebitAmount() != null ? l.getDebitAmount() : BigDecimal.ZERO;
                                BigDecimal cr = l.getCreditAmount() != null ? l.getCreditAmount() : BigDecimal.ZERO;
                                return dr.compareTo(BigDecimal.ZERO) > 0 ? dr : cr;
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", je.getId());
                    m.put("referenceNumber", je.getReferenceNumber());
                    m.put("entryDate", je.getEntryDate());
                    m.put("description", je.getDescription());
                    m.put("amount", amount);
                    m.put("referenceType", je.getReferenceType());
                    return m;
                })
                .collect(Collectors.toList());
    }

    public String generatePaymentAdviceHtml(UUID id) {
        BankVoucher v = findOrThrow(id);
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Payment Advice</title>")
          .append("<style>")
          .append("body{font-family:Arial,sans-serif;font-size:13px;margin:24px;color:#222}")
          .append("h2{text-align:center;margin:0 0 4px}p.sub{text-align:center;color:#555;margin:0 0 16px}")
          .append(com.erp.platform.modules.organization.service.CompanyLetterheadService.CSS)
          .append(".info-table{width:100%;border-collapse:collapse;margin-bottom:14px}")
          .append(".info-table td{padding:4px 8px;vertical-align:top}")
          .append(".info-table td:first-child{font-weight:bold;width:130px}")
          .append("table.lines{width:100%;border-collapse:collapse;margin-top:12px}")
          .append("table.lines th,table.lines td{border:1px solid #bbb;padding:5px 8px;text-align:left}")
          .append("table.lines th{background:#f0f0f0;font-weight:bold}")
          .append("table.lines th.amt,table.lines td.amt{text-align:right}")
          .append("table.lines td.amt{white-space:nowrap;font-variant-numeric:tabular-nums}")
          .append(".sig{margin-top:48px;display:flex;justify-content:space-between}")
          .append("</style></head><body>")
          .append(letterhead.html())
          .append("<h2>Payment Advice</h2>")
          .append("<table class='info-table'>")
          .append("<tr><td>Voucher No</td><td>").append(e(v.getVoucherNumber())).append("</td>")
          .append("<td>Date</td><td>").append(v.getVoucherDate()).append("</td></tr>")
          .append("<tr><td>Paid To</td><td>").append(e(v.getPartyName())).append("</td>")
          .append("<td>Mode</td><td>").append(e(v.getPaymentMode())).append("</td></tr>");
        if (v.getChequeNumber() != null && !v.getChequeNumber().isBlank()) {
            sb.append("<tr><td>Cheque No</td><td>").append(e(v.getChequeNumber())).append("</td>")
              .append("<td>Cheque Date</td><td>").append(v.getChequeDate()).append("</td></tr>");
        }
        if (v.getBankAccountName() != null) {
            sb.append("<tr><td>Bank Account</td><td colspan='3'>").append(e(v.getBankAccountName())).append("</td></tr>");
        }
        if (v.getNarration() != null) {
            sb.append("<tr><td>Narration</td><td colspan='3'>").append(e(v.getNarration())).append("</td></tr>");
        }
        sb.append("</table>")
          .append("<table class='lines'><thead><tr>")
          .append("<th>#</th><th>Account / Ledger</th><th>Bill No.</th><th class='amt'>Amount (₹)</th>")
          .append("</tr></thead><tbody>");
        int i = 1;
        for (BankVoucherLine line : v.getLines()) {
            sb.append("<tr>")
              .append("<td>").append(i++).append("</td>")
              .append("<td>").append(e(line.getLedgerAccountName())).append("</td>")
              .append("<td>").append(e(line.getBillNumber())).append("</td>")
              .append("<td class='amt'>").append(line.getAmount()).append("</td>")
              .append("</tr>");
        }
        sb.append("</tbody><tfoot><tr>")
          .append("<td colspan='3'><strong>Total</strong></td>")
          .append("<td class='amt'><strong>").append(v.getTotalAmount()).append("</strong></td>")
          .append("</tr></tfoot></table>")
          .append("<div class='sig'>")
          .append("<div><br><br><hr style='width:180px'>Authorised Signatory</div>")
          .append("<div><br><br><hr style='width:180px'>Received By / Stamp</div>")
          .append("</div></body></html>");
        return sb.toString();
    }

    private void postVoucher(BankVoucher voucher) {
        if (voucher.getBankAccountId() == null) {
            throw AppException.badRequest("Bank account is required to post the voucher");
        }
        Account bankAcct = accountRepository.findById(voucher.getBankAccountId())
                .orElseThrow(() -> AppException.notFound("Bank account not found: " + voucher.getBankAccountId()));

        JournalEntry je = new JournalEntry();
        je.setTenantId(voucher.getTenantId());
        je.setEntryDate(voucher.getVoucherDate());
        je.setReferenceType(voucher.getVoucherType() == VoucherType.RECEIPT ? "BANK_RECEIPT" : "BANK_PAYMENT");
        je.setReferenceId(voucher.getId());
        je.setReferenceNumber(voucher.getVoucherNumber());
        je.setDescription(voucher.getNarration() != null ? voucher.getNarration()
                : voucher.getPurpose() != null ? voucher.getPurpose()
                : voucher.getVoucherType().name() + " - " + voucher.getPartyName());

        // Compute bank-side amount from lines so the JE always balances
        BigDecimal linesTotal = voucher.getLines().stream()
                .filter(l -> l.getLedgerAccountId() != null
                        && l.getAmount() != null
                        && l.getAmount().compareTo(BigDecimal.ZERO) != 0)
                .map(BankVoucherLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Bank-side line
        JournalEntryLine bankLine = new JournalEntryLine();
        bankLine.setAccountId(bankAcct.getId());
        bankLine.setAccountCode(bankAcct.getCode());
        bankLine.setAccountName(bankAcct.getName());
        bankLine.setDescription(voucher.getVoucherNumber());
        if (voucher.getVoucherType() == VoucherType.RECEIPT) {
            bankLine.setDebitAmount(linesTotal);
            bankLine.setCreditAmount(BigDecimal.ZERO);
        } else {
            bankLine.setDebitAmount(BigDecimal.ZERO);
            bankLine.setCreditAmount(linesTotal);
        }
        je.getLines().add(bankLine);

        // Party/ledger lines
        for (BankVoucherLine line : voucher.getLines()) {
            if (line.getLedgerAccountId() == null
                    || line.getAmount() == null
                    || line.getAmount().compareTo(BigDecimal.ZERO) == 0) continue;
            JournalEntryLine jel = new JournalEntryLine();
            jel.setAccountId(line.getLedgerAccountId());
            jel.setAccountName(line.getLedgerAccountName());
            jel.setDescription(line.getBillNumber() != null
                    ? "Bill: " + line.getBillNumber()
                    : e(voucher.getPartyName()));
            if (voucher.getVoucherType() == VoucherType.RECEIPT) {
                jel.setDebitAmount(BigDecimal.ZERO);
                jel.setCreditAmount(line.getAmount());
            } else {
                jel.setDebitAmount(line.getAmount());
                jel.setCreditAmount(BigDecimal.ZERO);
            }
            je.getLines().add(jel);
        }

        JournalEntry saved = journalEntryService.create(je);
        voucher.setJournalEntryId(saved.getId());
        voucher.setStatus("POSTED");
        bankVoucherRepository.save(voucher);
        log.info("BankVoucher posted: id={}, number={}, JE={}", voucher.getId(), voucher.getVoucherNumber(), saved.getId());
    }

    private void mapFromRequest(BankVoucher v, CreateBankVoucherRequest r, UUID tenantId) {
        v.setVoucherType(r.getVoucherType());
        v.setVoucherDate(r.getVoucherDate() != null ? r.getVoucherDate() : LocalDate.now());
        v.setPartyName(r.getPartyName());
        v.setPurpose(r.getPurpose());
        v.setNarration(r.getNarration());
        v.setBankAccountId(r.getBankAccountId());
        v.setBankAccountName(r.getBankAccountName());
        v.setTotalAmount(r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO);
        v.setPaymentMode(r.getPaymentMode());
        v.setChequeNumber(r.getChequeNumber());
        v.setChequeDate(r.getChequeDate());
        v.setSingleChequePrint(r.isSingleChequePrint());
        v.setMultiCheque(r.isMultiCheque());
        v.setRtgs(r.isRtgs());
        v.setAcPayee(r.isAcPayee());
        v.setReferenceVoucherNumber(r.getReferenceVoucherNumber());
        v.setLocationName(r.getLocationName());
        v.getLines().clear();
        if (r.getLines() != null) {
            for (CreateBankVoucherRequest.LineRequest lr : r.getLines()) {
                if (lr.getLedgerAccountId() == null && (lr.getLedgerAccountName() == null || lr.getLedgerAccountName().isBlank()))
                    continue;
                BankVoucherLine line = new BankVoucherLine();
                line.setBankVoucher(v);
                line.setTenantId(tenantId);
                line.setAccountGroup(lr.getAccountGroup());
                line.setLedgerAccountId(lr.getLedgerAccountId());
                line.setLedgerAccountName(lr.getLedgerAccountName());
                line.setCostCenterId(lr.getCostCenterId());
                line.setCostCenterName(lr.getCostCenterName());
                line.setAmount(lr.getAmount() != null ? lr.getAmount() : BigDecimal.ZERO);
                line.setBillNumber(lr.getBillNumber());
                line.setLocationName(lr.getLocationName());
                line.setTransactionType(lr.getTransactionType());
                v.getLines().add(line);
            }
        }
    }

    public String generateBatchHtml(List<UUID> ids) {
        UUID tenantId = tenantContext.current();
        StringBuilder sb = new StringBuilder()
            .append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Vouchers</title>")
            .append("<style>")
            .append("body{font-family:Arial,sans-serif;font-size:13px;color:#222;margin:0;padding:0}")
            .append(".vpage{padding:24px 32px;max-width:760px;margin:0 auto}")
            .append("h2{text-align:center;margin:0 0 4px}p.sub{text-align:center;color:#555;margin:0 0 16px}")
            .append(".it{width:100%;border-collapse:collapse;margin-bottom:14px}")
            .append(".it td{padding:4px 8px;vertical-align:top}")
            .append(".it td:first-child{font-weight:bold;width:130px}")
            .append("table.ln{width:100%;border-collapse:collapse;margin-top:12px}")
            .append("table.ln th,table.ln td{border:1px solid #bbb;padding:5px 8px;text-align:left}")
            .append("table.ln th{background:#f0f0f0;font-weight:bold}")
            .append("table.ln th.amt,table.ln td.amt{text-align:right}")
            .append("table.ln td.amt{white-space:nowrap;font-variant-numeric:tabular-nums}")
            .append(".sig{margin-top:48px;display:flex;justify-content:space-between}")
            .append("</style></head><body>");

        for (int i = 0; i < ids.size(); i++) {
            BankVoucher v = bankVoucherRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, ids.get(i))
                    .orElseThrow(() -> AppException.notFound("Bank voucher not found"));
            sb.append("<div class='vpage'>")
              .append("<h2>Payment Advice</h2>")
              .append("<table class='it'>")
              .append("<tr><td>Voucher No</td><td>").append(e(v.getVoucherNumber())).append("</td>")
              .append("<td>Date</td><td>").append(v.getVoucherDate()).append("</td></tr>")
              .append("<tr><td>").append(v.getVoucherType() == VoucherType.RECEIPT ? "Received From" : "Paid To").append("</td><td>")
              .append(e(v.getPartyName())).append("</td><td>Mode</td><td>").append(e(v.getPaymentMode())).append("</td></tr>");
            if (v.getChequeNumber() != null && !v.getChequeNumber().isBlank())
                sb.append("<tr><td>Cheque No</td><td>").append(e(v.getChequeNumber()))
                  .append("</td><td>Cheque Date</td><td>").append(v.getChequeDate()).append("</td></tr>");
            if (v.getBankAccountName() != null)
                sb.append("<tr><td>Bank Account</td><td colspan='3'>").append(e(v.getBankAccountName())).append("</td></tr>");
            if (v.getNarration() != null)
                sb.append("<tr><td>Narration</td><td colspan='3'>").append(e(v.getNarration())).append("</td></tr>");
            sb.append("</table>")
              .append("<table class='ln'><thead><tr>")
              .append("<th>#</th><th>Account / Ledger</th><th>Bill No.</th><th class='amt'>Amount (&#8377;)</th>")
              .append("</tr></thead><tbody>");
            int j = 1;
            for (BankVoucherLine line : v.getLines())
                sb.append("<tr><td>").append(j++).append("</td><td>").append(e(line.getLedgerAccountName())).append("</td>")
                  .append("<td>").append(e(line.getBillNumber())).append("</td>")
                  .append("<td class='amt'>").append(line.getAmount()).append("</td></tr>");
            sb.append("</tbody><tfoot><tr><td colspan='3'><strong>Total</strong></td>")
              .append("<td class='amt'><strong>").append(v.getTotalAmount()).append("</strong></td></tr></tfoot></table>")
              .append("<div class='sig'>")
              .append("<div><br><br><hr style='width:180px;margin:0'>Authorised Signatory</div>")
              .append("<div><br><br><hr style='width:180px;margin:0'>Received By / Stamp</div>")
              .append("</div></div>");
            if (i < ids.size() - 1) sb.append("<div style='page-break-after:always'></div>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private BankVoucher findOrThrow(UUID id) {
        return bankVoucherRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Bank voucher not found: " + id));
    }

    private String generateNumber(VoucherType type) {
        String prefix = type == VoucherType.RECEIPT ? "BR" : "BP";
        String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String seq = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + ym + "-" + seq;
    }

    // ─── Cheque Generation ────────────────────────────────────────────────────

    public String generateChequeHtml(UUID id) {
        BankVoucher v = findOrThrow(id);

        // Fetch bank account for branch/IFSC details
        BankAccount bankAccount = v.getBankAccountId() != null
                ? bankAccountRepository.findById(v.getBankAccountId()).orElse(null) : null;
        String bankName   = bankAccount != null ? bankAccount.getBankName()   : (v.getBankAccountName() != null ? v.getBankAccountName() : "");
        String branch     = bankAccount != null && bankAccount.getBranch() != null ? bankAccount.getBranch() : "";
        String acctNumber = bankAccount != null ? bankAccount.getAccountNumber() : "";

        // Format date (DD   MM   YYYY for date-box style)
        LocalDate cheqDate = v.getChequeDate() != null ? v.getChequeDate() : v.getVoucherDate();
        String dd   = cheqDate != null ? String.format("%02d", cheqDate.getDayOfMonth()) : "";
        String mm   = cheqDate != null ? String.format("%02d", cheqDate.getMonthValue()) : "";
        String yyyy = cheqDate != null ? String.valueOf(cheqDate.getYear()) : "";

        String payee       = e(v.getPartyName());
        BigDecimal amount  = v.getTotalAmount() != null ? v.getTotalAmount() : BigDecimal.ZERO;
        String amtFigures  = String.format("%.2f", amount);
        String amtWords    = amountInWords(amount);
        String chequeNo    = e(v.getChequeNumber());
        boolean acPayee    = v.isAcPayee();

        return buildChequeHtml(bankName, branch, acctNumber, chequeNo,
                dd, mm, yyyy, payee, amtFigures, amtWords, acPayee,
                v.getVoucherNumber());
    }

    private String buildChequeHtml(String bankName, String branch, String acctNumber,
            String chequeNo, String dd, String mm, String yyyy,
            String payee, String amtFigures, String amtWords,
            boolean acPayee, String voucherNumber) {

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Cheque – " + e(voucherNumber) + "</title>"
            + "<style>"
            + "* { box-sizing: border-box; margin: 0; padding: 0; }"
            + "body { font-family: Arial, sans-serif; background: #f0f0f0; }"
            + "@media print {"
            + "  @page { size: 215mm 90mm; margin: 0; }"
            + "  body { background: transparent; }"
            + "  .page-wrapper { padding: 0; }"
            + "  .cheque { box-shadow: none; border: none; }"
            + "  .no-print { display: none !important; }"
            + "}"
            + ".page-wrapper { display: flex; flex-direction: column; align-items: center;"
            + "  justify-content: center; min-height: 100vh; padding: 24px; }"
            + ".no-print { margin-bottom: 16px; text-align: center; }"
            + ".no-print button { padding: 8px 24px; background: #1e40af; color: white;"
            + "  border: none; border-radius: 4px; font-size: 14px; cursor: pointer; margin: 0 4px; }"
            + ".no-print button:hover { background: #1e3a8a; }"
            + ".meta { font-size: 12px; color: #666; margin-bottom: 8px; }"
            + ".cheque {"
            + "  width: 215mm; height: 90mm; position: relative;"
            + "  background: white; border: 2px solid #aaa;"
            + "  box-shadow: 0 4px 20px rgba(0,0,0,0.2);"
            + "  font-size: 13px; overflow: hidden;"
            + "}"
            + ".cheque-header {"
            + "  position: absolute; top: 5mm; left: 5mm; right: 5mm;"
            + "  display: flex; justify-content: space-between; align-items: flex-start;"
            + "}"
            + ".bank-info h3 { font-size: 14px; font-weight: 700; color: #1e3a8a; }"
            + ".bank-info p  { font-size: 11px; color: #555; }"
            + (acPayee ? ".crossing { position: absolute; top: 3mm; left: 4mm;"
                + "  width: 16mm; height: 16mm; border-left: 2px solid #333; border-right: 2px solid #333;"
                + "  transform: rotate(-15deg); display: flex; flex-direction: column;"
                + "  align-items: center; justify-content: center; gap: 1mm; }"
                + ".crossing span { font-size: 8px; font-weight: 700; color: #333;"
                + "  white-space: nowrap; writing-mode: horizontal-tb; }" : "")
            + ".date-box {"
            + "  display: flex; align-items: center; gap: 1mm;"
            + "  border: 1px solid #aaa; padding: 1.5mm 3mm;"
            + "  font-size: 12px; font-family: monospace; background: #fafafa;"
            + "  border-radius: 2px;"
            + "}"
            + ".date-part { font-size: 14px; font-weight: 700; color: #111; min-width: 8mm; text-align: center; }"
            + ".date-sep  { font-weight: 700; color: #666; }"
            + ".field-row {"
            + "  position: absolute; left: 5mm; right: 5mm;"
            + "  display: flex; align-items: baseline; gap: 2mm;"
            + "}"
            + ".field-label { font-size: 11px; color: #555; white-space: nowrap; min-width: 14mm; }"
            + ".field-value {"
            + "  flex: 1; border-bottom: 1px solid #555; font-size: 13px; font-weight: 600;"
            + "  color: #111; padding-bottom: 1mm; min-height: 6mm;"
            + "  word-break: break-all;"
            + "}"
            + ".amt-box {"
            + "  position: absolute; right: 5mm; width: 38mm;"
            + "  border: 2px solid #1e3a8a; border-radius: 3px;"
            + "  padding: 2mm 3mm; text-align: right;"
            + "  background: #f0f4ff;"
            + "}"
            + ".amt-box .label { font-size: 9px; color: #555; letter-spacing: 0.05em; }"
            + ".amt-box .value { font-size: 16px; font-weight: 800; color: #1e3a8a; font-family: monospace; }"
            + ".rupee-sym { font-size: 13px; }"
            + ".sig-line {"
            + "  position: absolute; bottom: 8mm; right: 5mm; text-align: right;"
            + "}"
            + ".sig-line .line { border-top: 1px solid #555; width: 45mm; margin-bottom: 1mm; }"
            + ".sig-line .lbl  { font-size: 9px; color: #666; }"
            + ".micr-bar {"
            + "  position: absolute; bottom: 2mm; left: 5mm; right: 5mm;"
            + "  display: flex; justify-content: space-between; align-items: center;"
            + "  border-top: 1px dashed #bbb; padding-top: 1mm;"
            + "}"
            + ".micr-bar span { font-size: 10px; color: #888; letter-spacing: 0.05em; }"
            + ".cheque-no-big { font-size: 11px; font-weight: 700; color: #333; font-family: monospace; letter-spacing: 0.1em; }"
            + "</style></head><body>"
            + "<div class='page-wrapper'>"
            + "<div class='no-print'>"
            + "<div class='meta'>Voucher: " + e(voucherNumber) + " &nbsp;|&nbsp; Cheque No: " + e(chequeNo) + "</div>"
            + "<button onclick='window.print()'>&#128438; Print Cheque</button>"
            + "<button onclick='window.close()'>Close</button>"
            + "</div>"
            + "<div class='cheque'>"

            // Crossing (A/C Payee)
            + (acPayee ? "<div class='crossing'><span>A/C</span><span>Payee</span></div>" : "")

            // Header: bank info left, date right
            + "<div class='cheque-header'>"
            + "<div class='bank-info'>"
            + "<h3>" + e(bankName) + "</h3>"
            + (branch.isBlank() ? "" : "<p>Branch: " + e(branch) + "</p>")
            + (acctNumber.isBlank() ? "" : "<p>A/C No: " + e(acctNumber) + "</p>")
            + "</div>"
            + "<div>"
            + "<div style='font-size:9px;color:#888;margin-bottom:1mm;text-align:right'>Date</div>"
            + "<div class='date-box'>"
            + "<span class='date-part'>" + dd + "</span>"
            + "<span class='date-sep'>/</span>"
            + "<span class='date-part'>" + mm + "</span>"
            + "<span class='date-sep'>/</span>"
            + "<span class='date-part'>" + yyyy + "</span>"
            + "</div></div></div>"

            // Pay line
            + "<div class='field-row' style='top:28mm'>"
            + "<span class='field-label'>Pay</span>"
            + "<span class='field-value'>" + e(payee) + "</span>"
            + "</div>"

            // Amount words + figure box
            + "<div class='field-row' style='top:40mm;right:48mm'>"
            + "<span class='field-label'>Rupees</span>"
            + "<span class='field-value' style='font-size:12px'>" + e(amtWords) + "</span>"
            + "</div>"

            + "<div class='amt-box' style='top:33mm'>"
            + "<div class='label'>Amount (₹)</div>"
            + "<div class='value'><span class='rupee-sym'>₹</span> " + e(amtFigures) + "</div>"
            + "</div>"

            // Signature line
            + "<div class='sig-line'>"
            + "<div class='line'></div>"
            + "<div class='lbl'>Authorised Signatory</div>"
            + "</div>"

            // MICR / bottom bar
            + "<div class='micr-bar'>"
            + "<span>" + (acctNumber.isBlank() ? "" : "A/C: " + acctNumber) + "</span>"
            + "<span class='cheque-no-big'>" + (chequeNo.isBlank() ? "" : "Cheque No: " + e(chequeNo)) + "</span>"
            + "</div>"

            + "</div>"  // end .cheque
            + "</div>"  // end .page-wrapper
            + "</body></html>";
    }

    // ─── Amount in Words (Indian ₹ system) ────────────────────────────────────

    private static final String[] ONES = {
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    };
    private static final String[] TENS = {
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private String inWords(int n) {
        if (n == 0)   return "";
        if (n < 20)   return ONES[n];
        if (n < 100)  return TENS[n / 10] + (n % 10 != 0 ? " " + ONES[n % 10] : "");
        return ONES[n / 100] + " Hundred" + (n % 100 != 0 ? " " + inWords(n % 100) : "");
    }

    public String amountInWords(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return "Zero Rupees Only";
        long total    = amount.setScale(2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).longValue();
        long rupees   = total / 100;
        int  paise    = (int) (total % 100);

        if (rupees == 0 && paise == 0) return "Zero Rupees Only";

        long crores   = rupees / 10_000_000L;
        rupees       %= 10_000_000L;
        long lakhs    = rupees / 100_000L;
        rupees       %= 100_000L;
        long thousands = rupees / 1_000L;
        rupees       %= 1_000L;

        StringBuilder sb = new StringBuilder();
        if (crores    > 0) sb.append(inWords((int) crores)).append(" Crore ");
        if (lakhs     > 0) sb.append(inWords((int) lakhs)).append(" Lakh ");
        if (thousands > 0) sb.append(inWords((int) thousands)).append(" Thousand ");
        if (rupees    > 0) sb.append(inWords((int) rupees)).append(" ");

        sb.append("Rupees");
        if (paise > 0) sb.append(" and ").append(inWords(paise)).append(" Paise");
        sb.append(" Only");
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    private String e(String s) { return s != null ? s : ""; }

    public BankVoucherDto toDto(BankVoucher v) {
        BankVoucherDto dto = new BankVoucherDto();
        dto.setId(v.getId());
        dto.setTenantId(v.getTenantId());
        dto.setVoucherNumber(v.getVoucherNumber());
        dto.setVoucherType(v.getVoucherType());
        dto.setVoucherDate(v.getVoucherDate());
        dto.setPartyName(v.getPartyName());
        dto.setPurpose(v.getPurpose());
        dto.setNarration(v.getNarration());
        dto.setBankAccountId(v.getBankAccountId());
        dto.setBankAccountName(v.getBankAccountName());
        dto.setTotalAmount(v.getTotalAmount());
        dto.setPaymentMode(v.getPaymentMode());
        dto.setChequeNumber(v.getChequeNumber());
        dto.setChequeDate(v.getChequeDate());
        dto.setSingleChequePrint(v.isSingleChequePrint());
        dto.setMultiCheque(v.isMultiCheque());
        dto.setRtgs(v.isRtgs());
        dto.setAcPayee(v.isAcPayee());
        dto.setReferenceVoucherNumber(v.getReferenceVoucherNumber());
        dto.setStatus(v.getStatus());
        dto.setLocationName(v.getLocationName());
        dto.setJournalEntryId(v.getJournalEntryId());
        dto.setCreatedAt(v.getCreatedAt());
        if (v.getLines() != null) {
            dto.setLines(v.getLines().stream().map(this::toLineDto).collect(Collectors.toList()));
        }
        return dto;
    }

    private BankVoucherLineDto toLineDto(BankVoucherLine l) {
        BankVoucherLineDto dto = new BankVoucherLineDto();
        dto.setId(l.getId());
        dto.setAccountGroup(l.getAccountGroup());
        dto.setLedgerAccountId(l.getLedgerAccountId());
        dto.setLedgerAccountName(l.getLedgerAccountName());
        dto.setCostCenterId(l.getCostCenterId());
        dto.setCostCenterName(l.getCostCenterName());
        dto.setAmount(l.getAmount());
        dto.setBillNumber(l.getBillNumber());
        dto.setLocationName(l.getLocationName());
        dto.setTransactionType(l.getTransactionType());
        return dto;
    }
}

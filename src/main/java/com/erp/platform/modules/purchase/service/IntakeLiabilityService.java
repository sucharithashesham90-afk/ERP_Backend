package com.erp.platform.modules.purchase.service;

import com.erp.platform.modules.intake.entity.IntakeSlip;
import com.erp.platform.modules.intake.entity.IntakeSlipItem;
import com.erp.platform.modules.purchase.entity.PaymentLiability;
import com.erp.platform.modules.purchase.repository.PaymentLiabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Raises what is owed to a grower when their intake is completed.
 *
 * <p>This was the missing step in the agriculture flow. Liabilities could be paid — the screen, the
 * voucher, the ledger posting all worked — but nothing ever created one, so the only way to get a
 * row was to POST it by hand. In practice the screen was permanently empty and the whole chain from
 * intake to voucher stopped at the intake.
 *
 * <p>Deliberately forgiving about what it needs. An intake with no priced items still produces a
 * liability at zero rather than nothing at all: the goods arrived and someone is owed for them, and
 * a row showing zero is a prompt to enter the rate, where no row at all is invisible.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntakeLiabilityService {

    private final PaymentLiabilityRepository repository;

    /**
     * Create the liability for a completed intake slip, once.
     *
     * <p>Keyed on the slip number so completing an intake twice — or a retry after a failure
     * partway through — cannot raise a second claim for the same delivery.
     */
    @Transactional
    public void raiseFor(IntakeSlip slip) {
        if (slip == null || slip.getTenantId() == null) return;

        String liabilityNumber = "LIA-" + slip.getSlipNumber();
        if (exists(slip.getTenantId(), liabilityNumber)) {
            log.info("Liability {} already raised for intake {}", liabilityNumber, slip.getSlipNumber());
            return;
        }

        // The grower who grew it is who gets paid. Falling back to the vendor covers a third-party
        // intake, where the delivery came from a supplier rather than from a field.
        boolean fromGrower = hasText(slip.getFieldProducerName());
        String partyName = fromGrower ? slip.getFieldProducerName() : slip.getVendorName();
        if (!hasText(partyName)) {
            log.warn("Intake {} completed with no grower or vendor named; no liability raised",
                    slip.getSlipNumber());
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        if (slip.getItems() != null) {
            for (IntakeSlipItem item : slip.getItems()) {
                BigDecimal qty = nz(item.getAcceptedQuantity());
                // What was accepted is what is owed for; rejected quantity is not.
                total = total.add(qty.multiply(nz(item.getUnitPrice())));
            }
        }

        PaymentLiability liability = new PaymentLiability();
        liability.setTenantId(slip.getTenantId());
        liability.setLiabilityNumber(liabilityNumber);
        liability.setPartyType(fromGrower ? "GROWER" : "SUPPLIER");
        liability.setPartyName(partyName);
        // A lot number is also what tells the Liability Payment screen this was a lot-wise intake
        // rather than a truck-wise one; the filter there derives the type from its presence.
        liability.setLotNumber(hasText(slip.getLotNumber()) ? slip.getLotNumber() : slip.getTemporaryLotNumber());
        liability.setLiabilityFromDate(slip.getReceiptDate());
        liability.setLiabilityToDate(slip.getReceiptDate());
        liability.setTotalLiability(total);
        liability.setPaidAmount(BigDecimal.ZERO);
        liability.setBalance(total);
        liability.setStatus("PENDING");

        repository.save(liability);
        log.info("Liability {} raised for {} ({}) from intake {}: {}",
                liabilityNumber, partyName, liability.getPartyType(), slip.getSlipNumber(), total);
    }

    /** Whether this intake has already produced a liability. */
    public boolean exists(UUID tenantId, String liabilityNumber) {
        return repository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 20000))
                .getContent().stream()
                .anyMatch(l -> liabilityNumber.equalsIgnoreCase(l.getLiabilityNumber()));
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}

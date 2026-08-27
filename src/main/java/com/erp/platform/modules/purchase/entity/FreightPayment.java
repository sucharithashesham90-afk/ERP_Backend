package com.erp.platform.modules.purchase.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "freight_payments",
       indexes = {@Index(name = "idx_frtpay_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class FreightPayment extends TenantEntity {

    @Column(name = "payment_number", nullable = false, length = 50)
    private String paymentNumber;

    @Column(name = "goods_receipt_id")
    private UUID goodsReceiptId;

    @Column(name = "grn_number", length = 50)
    private String grnNumber;

    @Column(name = "carrier_name", length = 200)
    private String carrierName;

    @Column(name = "lr_number", length = 50)
    private String lrNumber;

    @Column(name = "dc_number", length = 50)
    private String dcNumber;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "freight_amount", precision = 18, scale = 2)
    private BigDecimal freightAmount = BigDecimal.ZERO;

    @Column(name = "advance_paid", precision = 18, scale = 2)
    private BigDecimal advancePaid = BigDecimal.ZERO;

    @Column(name = "amount_paid", precision = 18, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "balance_due", precision = 18, scale = 2)
    private BigDecimal balanceDue = BigDecimal.ZERO;

    @Column(name = "payment_mode", length = 20)
    private String paymentMode;

    @Column(name = "cheque_number", length = 30)
    private String chequeNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(length = 20)
    private String status = "DRAFT";

    @Column(length = 500)
    private String notes;
}

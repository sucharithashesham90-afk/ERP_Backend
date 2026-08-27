package com.erp.platform.modules.dispatch.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dispatches",
        indexes = {
                @Index(name = "idx_dispatch_tenant", columnList = "tenant_id"),
                @Index(name = "idx_dispatch_status", columnList = "tenant_id, status"),
                @Index(name = "idx_dispatch_customer", columnList = "tenant_id, customer_id")
        })
@Getter
@Setter
public class Dispatch extends TenantEntity {

    @Column(name = "dispatch_number", nullable = false, length = 50)
    private String dispatchNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispatch_type", length = 30)
    private DispatchType dispatchType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DispatchStatus status = DispatchStatus.DRAFT;

    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "delivery_order_ref", length = 100)
    private String deliveryOrderRef;

    @Column(name = "challan_value", precision = 18, scale = 2)
    private BigDecimal challanValue = BigDecimal.ZERO;

    @Column(name = "billing_address", length = 500)
    private String billingAddress;

    @Column(name = "billing_phone", length = 30)
    private String billingPhone;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "delivery_phone", length = 30)
    private String deliveryPhone;

    @Column(name = "consignee_buyer_same")
    private Boolean consigneeBuyerSame = true;

    @Column(name = "sales_office", length = 100)
    private String salesOffice;

    @Column(name = "from_location", length = 200)
    private String fromLocation;

    @Column(name = "to_location", length = 200)
    private String toLocation;

    @Column(name = "stock_transfer_order_ref", length = 100)
    private String stockTransferOrderRef;

    @Column(name = "dispatch_date")
    private LocalDate dispatchDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "carrier_name", length = 200)
    private String carrierName;

    @Column(name = "carrier_phone", length = 20)
    private String carrierPhone;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "lr_number", length = 100)
    private String lrNumber;

    @Column(name = "way_bill_number", length = 100)
    private String wayBillNumber;

    @Column(name = "rr_rl_number", length = 100)
    private String rrRlNumber;

    @Column(name = "freight_charges", precision = 18, scale = 2)
    private BigDecimal freightCharges = BigDecimal.ZERO;

    @Column(name = "freight_paid_advance", precision = 18, scale = 2)
    private BigDecimal freightPaidAdvance = BigDecimal.ZERO;

    @Column(name = "freight_to_pay", precision = 18, scale = 2)
    private BigDecimal freightToPay = BigDecimal.ZERO;

    @Column(name = "balance_after_submission", precision = 18, scale = 2)
    private BigDecimal balanceAfterSubmission = BigDecimal.ZERO;

    @Column(name = "total_packages", nullable = false, columnDefinition = "integer not null default 0")
    private int totalPackages;

    @Column(name = "total_weight", precision = 18, scale = 3)
    private BigDecimal totalWeight = BigDecimal.ZERO;

    @Column(name = "weight_unit", length = 10)
    private String weightUnit = "KG";

    @Column(name = "packed_by", length = 200)
    private String packedBy;

    @Column(name = "transporter_pan_number", length = 20)
    private String transporterPanNumber;

    @Column(name = "documents_through", length = 200)
    private String documentsThrough;

    @Column(name = "pod_received")
    private Boolean podReceived = false;

    @Column(name = "pod_date")
    private LocalDate podDate;

    @Column(name = "pod_reference", length = 100)
    private String podReference;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "dispatch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DispatchItem> items = new ArrayList<>();

    public enum DispatchType {
        CUSTOMER, STOCK_TRANSFER, INTER_BRANCH, RETURN_TO_VENDOR, SAMPLE
    }

    public enum DispatchStatus {
        DRAFT, PACKED, DISPATCHED, IN_TRANSIT, DELIVERED, RETURNED
    }
}

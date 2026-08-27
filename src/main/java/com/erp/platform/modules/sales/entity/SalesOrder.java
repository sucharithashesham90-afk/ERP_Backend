package com.erp.platform.modules.sales.entity;

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
@Table(name = "sales_orders",
       indexes = {
           @Index(name = "idx_so_tenant", columnList = "tenant_id"),
           @Index(name = "idx_so_status", columnList = "tenant_id, status")
       })
@Getter
@Setter
public class SalesOrder extends TenantEntity {

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "quotation_id")
    private UUID quotationId;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private SalesOrderStatus status = SalesOrderStatus.DRAFT;

    @Column(precision = 18, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    /** Where the order ships to, chosen from the geography masters rather than typed. */
    @Column(name = "shipping_state", length = 100)
    private String shippingState;

    @Column(name = "shipping_district", length = 100)
    private String shippingDistrict;

    @Column(name = "shipping_city", length = 100)
    private String shippingCity;

    @Column(name = "shipping_country", length = 100)
    private String shippingCountry;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(length = 1000)
    private String notes;

    // -- Signed on the spot -------------------------------------------------
    //
    // A dealer signing on the rep's phone is the moment the order becomes theirs. Held as a data
    // URI on the order itself rather than a separate document, because a signature that can drift
    // apart from the order it authorises is worth little.

    @Column(name = "signature_image", columnDefinition = "text")
    private String signatureImage;

    @Column(name = "signed_by", length = 200)
    private String signedBy;

    @Column(name = "signed_at")
    private java.time.LocalDateTime signedAt;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesOrderItem> items = new ArrayList<>();

    public enum SalesOrderStatus {
        DRAFT, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }
}

package com.ecoshop.checkout.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "checkout_sessions", indexes = {
        @Index(name = "idx_checkout_user", columnList = "user_id"),
        @Index(name = "idx_checkout_status", columnList = "status"),
        @Index(name = "idx_checkout_idem", columnList = "idempotency_key", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CheckoutSession extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "cart_key", nullable = false, length = 100)
    private String cartKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private CheckoutStatus status = CheckoutStatus.STARTED;

    /** Idempotency key for the whole checkout (client-supplied). */
    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount", precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(name = "tax", precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(name = "shipping_cost", precision = 12, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_number", length = 50)
    private String orderNumber;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "payment_gateway_ref", length = 100)
    private String paymentGatewayRef;

    @Column(name = "client_secret", length = 200)
    private String clientSecret;

    @Column(name = "checkout_url", length = 500)
    private String checkoutUrl;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    /** Snapshot of shipping address used (denormalized to survive address changes). */
    @Column(name = "ship_recipient_name", length = 200)
    private String shipRecipientName;
    @Column(name = "ship_phone", length = 20)
    private String shipPhone;
    @Column(name = "ship_line1", length = 255)
    private String shipLine1;
    @Column(name = "ship_line2", length = 255)
    private String shipLine2;
    @Column(name = "ship_city", length = 100)
    private String shipCity;
    @Column(name = "ship_state", length = 100)
    private String shipState;
    @Column(name = "ship_postal_code", length = 20)
    private String shipPostalCode;
    @Column(name = "ship_country", length = 2)
    private String shipCountry;
}

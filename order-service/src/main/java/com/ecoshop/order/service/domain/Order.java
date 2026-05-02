package com.ecoshop.order.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_number", columnList = "order_number", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Order extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Human-readable order number, e.g. ORD-2026-0001234 */
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "tax", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "shipping_cost", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    /** Snapshot of shipping address (denormalized to survive address edits) */
    @Embedded
    private ShippingAddress shippingAddress;

    @Column(name = "payment_id", length = 100)
    private String paymentId; // payment-service ref

    @Column(name = "payment_status", length = 32)
    private String paymentStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public void addItem(OrderItem i) { items.add(i); i.setOrder(this); }
    public void addStatusHistory(OrderStatusHistory h) { statusHistory.add(h); h.setOrder(this); }
}

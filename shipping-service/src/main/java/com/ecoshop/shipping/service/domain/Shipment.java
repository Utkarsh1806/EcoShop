package com.ecoshop.shipping.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipments_order", columnList = "order_id", unique = true),
        @Index(name = "idx_shipments_tracking", columnList = "tracking_number"),
        @Index(name = "idx_shipments_status", columnList = "status")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "courier", nullable = false, length = 50)
    private String courier;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "label_url", length = 1000)
    private String labelUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @Column(name = "shipping_cost", precision = 12, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "estimated_delivery")
    private Instant estimatedDelivery;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    // Destination address snapshot
    @Column(name = "to_recipient_name", length = 200)
    private String toRecipientName;

    @Column(name = "to_phone", length = 20)
    private String toPhone;

    @Column(name = "to_line1", length = 255)
    private String toLine1;

    @Column(name = "to_line2", length = 255)
    private String toLine2;

    @Column(name = "to_city", length = 100)
    private String toCity;

    @Column(name = "to_state", length = 100)
    private String toState;

    @Column(name = "to_postal_code", length = 20)
    private String toPostalCode;

    @Column(name = "to_country", length = 2)
    private String toCountry;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrackingEvent> trackingEvents = new ArrayList<>();

    public void addEvent(TrackingEvent e) { trackingEvents.add(e); e.setShipment(this); }
}

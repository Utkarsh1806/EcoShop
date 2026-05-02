package com.ecoshop.shipping.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tracking_events", indexes = {
        @Index(name = "idx_tracking_shipment", columnList = "shipment_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TrackingEvent extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ShipmentStatus status;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(length = 200)
    private String location;

    @Column(length = 500)
    private String description;
}

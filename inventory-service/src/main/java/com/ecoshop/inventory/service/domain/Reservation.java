package com.ecoshop.inventory.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservations_ref", columnList = "reference_id"),
        @Index(name = "idx_reservations_status", columnList = "status")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Reservation extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** External reference - usually orderId or checkoutId */
    @Column(name = "reference_id", nullable = false, length = 100)
    private String referenceId;

    @Column(name = "reference_type", nullable = false, length = 32)
    private String referenceType; // ORDER | CHECKOUT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.HELD;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReservationItem> items = new ArrayList<>();

    public void addItem(ReservationItem i) { items.add(i); i.setReservation(this); }
}

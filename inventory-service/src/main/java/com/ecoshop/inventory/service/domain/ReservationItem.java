package com.ecoshop.inventory.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "reservation_items", indexes = {
        @Index(name = "idx_reservation_items_resv", columnList = "reservation_id"),
        @Index(name = "idx_reservation_items_stock", columnList = "stock_item_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "stock_item_id", nullable = false)
    private UUID stockItemId;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false)
    private int quantity;
}

package com.ecoshop.order.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "order_status_history", indexes = {
        @Index(name = "idx_status_history_order", columnList = "order_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderStatusHistory extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 32)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 32)
    private OrderStatus toStatus;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "changed_by", length = 100)
    private String changedBy; // userId, system, webhook
}

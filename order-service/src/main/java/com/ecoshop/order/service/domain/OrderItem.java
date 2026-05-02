package com.ecoshop.order.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order", columnList = "order_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 300)
    private String productName;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;
}

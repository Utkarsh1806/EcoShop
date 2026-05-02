package com.ecoshop.returns.service.domain;

import com.ecoshop.common.persistence.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "return_items", indexes = {
        @Index(name = "idx_return_items_request", columnList = "return_request_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ReturnItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_request_id", nullable = false)
    private ReturnRequest returnRequest;

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "product_name", length = 300)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_refund_amount", precision = 12, scale = 2)
    private BigDecimal lineRefundAmount;
}

package com.ecoshop.cart.service.domain;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem implements Serializable {

    private UUID productId;
    private UUID variantId; // optional, may be null
    private String sku;
    private String name;
    private String thumbnailUrl;

    /** Snapshot of unit price at time of add */
    private BigDecimal unitPrice;

    private int quantity;

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

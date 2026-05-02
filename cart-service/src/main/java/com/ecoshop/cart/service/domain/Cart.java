package com.ecoshop.cart.service.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Cart implements Serializable {

    /** Cart key — userId for logged-in users, generated UUID for guests */
    private String cartKey;

    /** Owning userId, null for guest carts */
    private UUID userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private String couponCode;

    @Builder.Default
    private String currency = "INR";

    private Instant createdAt;
    private Instant updatedAt;

    public BigDecimal subtotal() {
        return items.stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int itemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}

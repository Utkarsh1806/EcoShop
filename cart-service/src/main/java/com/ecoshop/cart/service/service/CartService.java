package com.ecoshop.cart.service.service;

import com.ecoshop.cart.service.client.ProductCatalogClient;
import com.ecoshop.cart.service.client.ProductCatalogClient.ProductView;
import com.ecoshop.cart.service.client.ProductCatalogClient.VariantView;
import com.ecoshop.cart.service.domain.Cart;
import com.ecoshop.cart.service.domain.CartItem;
import com.ecoshop.cart.service.dto.CartDtos.*;
import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.common.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private static final Duration CART_TTL = Duration.ofDays(30);
    private static final String KEY_PREFIX = "cart:";

    private final RedisTemplate<String, Cart> redis;
    private final ProductCatalogClient catalogClient;

    public CartResponse getCart(String cartKey) {
        Cart cart = loadOrInit(cartKey, null);
        return toResponse(cart);
    }

    public CartResponse addItem(String cartKey, UUID userId, AddItemRequest req) {
        Cart cart = loadOrInit(cartKey, userId);

        ProductView product = fetchProduct(req.productId());
        BigDecimal unitPrice = resolvePrice(product, req.variantId());
        VariantView variant = req.variantId() == null ? null :
                product.variants().stream()
                        .filter(v -> v.id().equals(req.variantId()))
                        .findFirst()
                        .orElseThrow(() -> BusinessException.notFound("VARIANT_NOT_FOUND",
                                "Variant " + req.variantId() + " not on product"));

        // If item already in cart, increment qty; else add new
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(req.productId())
                        && java.util.Objects.equals(i.getVariantId(), req.variantId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + req.quantity());
            existing.get().setUnitPrice(unitPrice);
        } else {
            String thumbnail = product.images() == null || product.images().isEmpty() ?
                    null : product.images().get(0).url();
            cart.getItems().add(CartItem.builder()
                    .productId(req.productId())
                    .variantId(req.variantId())
                    .sku(variant != null ? variant.sku() : null)
                    .name(product.name())
                    .thumbnailUrl(thumbnail)
                    .unitPrice(unitPrice)
                    .quantity(req.quantity())
                    .build());
        }
        return persistAndReturn(cart);
    }

    public CartResponse updateQuantity(String cartKey, UUID productId, UUID variantId, int quantity) {
        Cart cart = loadOrInit(cartKey, null);
        if (quantity == 0) {
            cart.getItems().removeIf(i -> i.getProductId().equals(productId)
                    && java.util.Objects.equals(i.getVariantId(), variantId));
        } else {
            CartItem item = cart.getItems().stream()
                    .filter(i -> i.getProductId().equals(productId)
                            && java.util.Objects.equals(i.getVariantId(), variantId))
                    .findFirst()
                    .orElseThrow(() -> BusinessException.notFound("CART_ITEM_NOT_FOUND",
                            "Item not in cart"));
            item.setQuantity(quantity);
        }
        return persistAndReturn(cart);
    }

    public CartResponse removeItem(String cartKey, UUID productId, UUID variantId) {
        Cart cart = loadOrInit(cartKey, null);
        cart.getItems().removeIf(i -> i.getProductId().equals(productId)
                && java.util.Objects.equals(i.getVariantId(), variantId));
        return persistAndReturn(cart);
    }

    public CartResponse applyCoupon(String cartKey, String code) {
        Cart cart = loadOrInit(cartKey, null);
        // Real implementation: call pricing-promotion-service to validate
        // For now, accept any non-empty code as a placeholder
        cart.setCouponCode(code);
        return persistAndReturn(cart);
    }

    public void clear(String cartKey) {
        redis.delete(KEY_PREFIX + cartKey);
    }

    /**
     * Merge a guest cart into a user cart on login. Items from guest cart are added; if duplicate,
     * quantities are summed. Guest cart is deleted.
     */
    public CartResponse mergeGuestIntoUser(String guestCartKey, UUID userId) {
        Cart guest = redis.opsForValue().get(KEY_PREFIX + guestCartKey);
        Cart user = loadOrInit(userId.toString(), userId);
        if (guest == null || guest.getItems().isEmpty()) {
            return toResponse(user);
        }
        for (CartItem g : guest.getItems()) {
            Optional<CartItem> existing = user.getItems().stream()
                    .filter(i -> i.getProductId().equals(g.getProductId())
                            && java.util.Objects.equals(i.getVariantId(), g.getVariantId()))
                    .findFirst();
            if (existing.isPresent()) {
                existing.get().setQuantity(existing.get().getQuantity() + g.getQuantity());
            } else {
                user.getItems().add(g);
            }
        }
        redis.delete(KEY_PREFIX + guestCartKey);
        log.info("Merged guest cart {} into user {}", guestCartKey, userId);
        return persistAndReturn(user);
    }

    // ─── Internals ───

    private Cart loadOrInit(String cartKey, UUID userId) {
        Cart cart = redis.opsForValue().get(KEY_PREFIX + cartKey);
        if (cart == null) {
            cart = Cart.builder()
                    .cartKey(cartKey)
                    .userId(userId)
                    .currency("INR")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }
        return cart;
    }

    private CartResponse persistAndReturn(Cart cart) {
        cart.setUpdatedAt(Instant.now());
        redis.opsForValue().set(KEY_PREFIX + cart.getCartKey(), cart, CART_TTL);
        return toResponse(cart);
    }

    @CircuitBreaker(name = "default", fallbackMethod = "fetchProductFallback")
    public ProductView fetchProduct(UUID productId) {
        ApiResponse<ProductView> resp = catalogClient.getById(productId);
        if (resp == null || !resp.success() || resp.data() == null) {
            throw BusinessException.notFound("PRODUCT_NOT_FOUND",
                    "Product " + productId + " not found in catalog");
        }
        return resp.data();
    }

    @SuppressWarnings("unused")
    public ProductView fetchProductFallback(UUID productId, Throwable t) {
        log.error("Catalog service unavailable for product {}: {}", productId, t.getMessage());
        throw BusinessException.badRequest("CATALOG_UNAVAILABLE",
                "Could not verify product right now, please retry");
    }

    private BigDecimal resolvePrice(ProductView product, UUID variantId) {
        if (variantId == null) return product.basePrice();
        return product.variants().stream()
                .filter(v -> v.id().equals(variantId))
                .findFirst()
                .map(v -> v.price() != null ? v.price() : product.basePrice())
                .orElseThrow(() -> BusinessException.notFound("VARIANT_NOT_FOUND",
                        "Variant " + variantId + " not on product"));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(i.getProductId(), i.getVariantId(),
                        i.getSku(), i.getName(), i.getThumbnailUrl(),
                        i.getUnitPrice(), i.getQuantity(), i.lineTotal()))
                .toList();
        return new CartResponse(
                cart.getCartKey(), cart.getUserId(), items, cart.getCouponCode(),
                cart.getCurrency(), cart.subtotal(), cart.itemCount()
        );
    }
}

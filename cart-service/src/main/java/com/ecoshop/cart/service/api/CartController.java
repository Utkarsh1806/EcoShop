package com.ecoshop.cart.service.api;

import com.ecoshop.cart.service.dto.CartDtos.*;
import com.ecoshop.cart.service.service.CartService;
import com.ecoshop.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Cart endpoints. Logged-in users use cartKey = userId. Guests pass an X-Cart-Key header
 * with a stable client-generated UUID. On login, frontend calls /merge to fold guest cart
 * into user cart.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal String userId,
                                             @RequestHeader(value = "X-Cart-Key", required = false) String guestKey) {
        return ApiResponse.ok(cartService.getCart(resolveKey(userId, guestKey)));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(@AuthenticationPrincipal String userId,
                                             @RequestHeader(value = "X-Cart-Key", required = false) String guestKey,
                                             @Valid @RequestBody AddItemRequest req) {
        UUID userUuid = userId != null ? UUID.fromString(userId) : null;
        return ApiResponse.ok(cartService.addItem(resolveKey(userId, guestKey), userUuid, req));
    }

    @PutMapping("/items/{productId}")
    public ApiResponse<CartResponse> updateQty(@AuthenticationPrincipal String userId,
                                               @RequestHeader(value = "X-Cart-Key", required = false) String guestKey,
                                               @PathVariable UUID productId,
                                               @RequestParam(required = false) UUID variantId,
                                               @Valid @RequestBody UpdateQuantityRequest req) {
        return ApiResponse.ok(cartService.updateQuantity(
                resolveKey(userId, guestKey), productId, variantId, req.quantity()));
    }

    @DeleteMapping("/items/{productId}")
    public ApiResponse<CartResponse> removeItem(@AuthenticationPrincipal String userId,
                                                @RequestHeader(value = "X-Cart-Key", required = false) String guestKey,
                                                @PathVariable UUID productId,
                                                @RequestParam(required = false) UUID variantId) {
        return ApiResponse.ok(cartService.removeItem(
                resolveKey(userId, guestKey), productId, variantId));
    }

    @PostMapping("/coupon")
    public ApiResponse<CartResponse> applyCoupon(@AuthenticationPrincipal String userId,
                                                 @RequestHeader(value = "X-Cart-Key", required = false) String guestKey,
                                                 @Valid @RequestBody ApplyCouponRequest req) {
        return ApiResponse.ok(cartService.applyCoupon(resolveKey(userId, guestKey), req.code()));
    }

    @DeleteMapping
    public ApiResponse<Void> clear(@AuthenticationPrincipal String userId,
                                   @RequestHeader(value = "X-Cart-Key", required = false) String guestKey) {
        cartService.clear(resolveKey(userId, guestKey));
        return ApiResponse.ok(null);
    }

    /** Called after login to fold guest cart into user cart. */
    @PostMapping("/merge")
    public ApiResponse<CartResponse> merge(@AuthenticationPrincipal String userId,
                                           @RequestHeader("X-Cart-Key") String guestKey) {
        if (userId == null) {
            throw new IllegalArgumentException("Authentication required to merge");
        }
        return ApiResponse.ok(cartService.mergeGuestIntoUser(guestKey, UUID.fromString(userId)));
    }

    /** Internal endpoint used by checkout service to read final cart state */
    @GetMapping("/internal/{cartKey}")
    public ApiResponse<CartResponse> getByKey(@PathVariable String cartKey) {
        return ApiResponse.ok(cartService.getCart(cartKey));
    }

    private String resolveKey(String userId, String guestKey) {
        if (userId != null) return userId;
        if (guestKey != null && !guestKey.isBlank()) return guestKey;
        throw new IllegalArgumentException("No cart key — provide X-Cart-Key header for guest carts");
    }
}

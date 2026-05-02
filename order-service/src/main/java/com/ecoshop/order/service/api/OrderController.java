package com.ecoshop.order.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.order.service.dto.OrderDtos.*;
import com.ecoshop.order.service.service.OrderManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderManagementService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> placeOrder(@AuthenticationPrincipal String userId,
                                                 @Valid @RequestBody PlaceOrderRequest req) {
        return ApiResponse.ok(orderService.placeOrder(UUID.fromString(userId), req));
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> listMyOrders(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(orderService.listForUser(UUID.fromString(userId), page, size));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getById(@AuthenticationPrincipal String userId,
                                              @PathVariable UUID orderId) {
        return ApiResponse.ok(orderService.getById(UUID.fromString(userId), orderId));
    }

    @GetMapping("/number/{orderNumber}")
    public ApiResponse<OrderResponse> getByNumber(@AuthenticationPrincipal String userId,
                                                  @PathVariable String orderNumber) {
        return ApiResponse.ok(orderService.getByNumber(UUID.fromString(userId), orderNumber));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancel(@AuthenticationPrincipal String userId,
                                             @PathVariable UUID orderId) {
        return ApiResponse.ok(orderService.cancel(UUID.fromString(userId), orderId));
    }

    /** Admin/internal — update status (e.g. shipped, delivered) */
    @PostMapping("/internal/{orderId}/status")
    public ApiResponse<OrderResponse> updateStatus(@PathVariable UUID orderId,
                                                   @Valid @RequestBody UpdateStatusRequest req,
                                                   @RequestHeader(value = "X-Actor", defaultValue = "system") String actor) {
        return ApiResponse.ok(orderService.updateStatus(orderId, req, actor));
    }

    /** Internal — payment service calls this on successful capture */
    @PostMapping("/internal/{orderId}/mark-paid")
    public ApiResponse<OrderResponse> markPaid(@PathVariable UUID orderId,
                                               @RequestParam String paymentId) {
        return ApiResponse.ok(orderService.markPaid(orderId, paymentId));
    }

    /** Internal — payment service calls this on failed capture */
    @PostMapping("/internal/{orderId}/mark-failed")
    public ApiResponse<OrderResponse> markFailed(@PathVariable UUID orderId,
                                                 @RequestParam(required = false) String reason) {
        return ApiResponse.ok(orderService.markFailed(orderId, reason));
    }
}

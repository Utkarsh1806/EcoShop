package com.ecoshop.shipping.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.shipping.service.dto.ShippingDtos.*;
import com.ecoshop.shipping.service.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/rates")
    public ApiResponse<RateQuoteResponse> rate(@Valid @RequestBody RateQuoteRequest req) {
        return ApiResponse.ok(shippingService.quoteRate(req));
    }

    @PostMapping("/shipments")
    public ApiResponse<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest req) {
        return ApiResponse.ok(shippingService.createShipment(req));
    }

    @GetMapping("/shipments/order/{orderId}")
    public ApiResponse<ShipmentResponse> getByOrder(@PathVariable UUID orderId) {
        return ApiResponse.ok(shippingService.getByOrderId(orderId));
    }

    @GetMapping("/shipments/track/{trackingNumber}")
    public ApiResponse<ShipmentResponse> track(@PathVariable String trackingNumber) {
        return ApiResponse.ok(shippingService.getByTrackingNumber(trackingNumber));
    }

    /** Internal — admin or courier-webhook updates shipment status */
    @PostMapping("/shipments/{shipmentId}/status")
    public ApiResponse<ShipmentResponse> updateStatus(@PathVariable UUID shipmentId,
                                                      @Valid @RequestBody UpdateStatusRequest req) {
        return ApiResponse.ok(shippingService.updateStatus(shipmentId, req));
    }

    @PostMapping("/shipments/{shipmentId}/cancel")
    public ApiResponse<ShipmentResponse> cancel(@PathVariable UUID shipmentId) {
        return ApiResponse.ok(shippingService.cancel(shipmentId));
    }
}

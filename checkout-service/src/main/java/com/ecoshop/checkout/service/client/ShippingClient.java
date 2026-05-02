package com.ecoshop.checkout.service.client;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.Instant;

@FeignClient(name = "shipping-service", path = "/api/shipping")
public interface ShippingClient {

    @PostMapping("/rates")
    ApiResponse<RateQuoteResponse> rate(@RequestBody RateQuoteRequest req);

    record RateQuoteRequest(String fromPincode, String toPincode, int weightGrams) {}
    record RateQuoteResponse(String courier, BigDecimal cost, Instant estimatedDelivery, int transitDays) {}
}

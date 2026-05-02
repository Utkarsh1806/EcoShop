package com.ecoshop.seller.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.seller.service.domain.SellerStatus;
import com.ecoshop.seller.service.dto.SellerDtos.*;
import com.ecoshop.seller.service.service.SellerManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerManagementService sellerService;

    // ─── Seller self-service ───
    @PostMapping
    public ApiResponse<SellerResponse> onboard(@AuthenticationPrincipal String userId,
                                               @Valid @RequestBody OnboardSellerRequest req) {
        return ApiResponse.ok(sellerService.onboard(UUID.fromString(userId), req));
    }

    @GetMapping("/me")
    public ApiResponse<SellerResponse> getMine(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(sellerService.getMyAccount(UUID.fromString(userId)));
    }

    @PostMapping("/me/products")
    public ApiResponse<ProductLinkResponse> submitProduct(@AuthenticationPrincipal String userId,
                                                          @Valid @RequestBody SubmitProductRequest req) {
        return ApiResponse.ok(sellerService.submitProduct(UUID.fromString(userId), req));
    }

    @GetMapping("/me/products")
    public ApiResponse<PageResponse<ProductLinkResponse>> listMyProducts(@AuthenticationPrincipal String userId,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(sellerService.listMyProducts(UUID.fromString(userId), page, size));
    }

    // ─── Public lookup ───
    @GetMapping("/{sellerId}")
    public ApiResponse<SellerResponse> getById(@PathVariable UUID sellerId) {
        return ApiResponse.ok(sellerService.getById(sellerId));
    }

    // ─── Admin endpoints ───
    @GetMapping("/admin")
    public ApiResponse<PageResponse<SellerResponse>> listByStatus(@RequestParam SellerStatus status,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(sellerService.listByStatus(status, page, size));
    }

    @PostMapping("/admin/{sellerId}/verify")
    public ApiResponse<SellerResponse> verify(@PathVariable UUID sellerId,
                                              @RequestHeader(value = "X-Actor", defaultValue = "admin") String actor,
                                              @Valid @RequestBody VerifySellerRequest req) {
        return ApiResponse.ok(sellerService.verify(sellerId, req, actor));
    }

    @PostMapping("/admin/{sellerId}/suspend")
    public ApiResponse<SellerResponse> suspend(@PathVariable UUID sellerId,
                                               @RequestHeader(value = "X-Actor", defaultValue = "admin") String actor,
                                               @RequestParam(required = false) String reason) {
        return ApiResponse.ok(sellerService.suspend(sellerId, reason, actor));
    }

    @GetMapping("/admin/products/pending")
    public ApiResponse<PageResponse<ProductLinkResponse>> listPendingProducts(@RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(sellerService.listPendingApprovals(page, size));
    }

    @PostMapping("/admin/products/{linkId}/approve")
    public ApiResponse<ProductLinkResponse> approveProduct(@PathVariable UUID linkId,
                                                           @RequestHeader(value = "X-Actor", defaultValue = "admin") String actor,
                                                           @Valid @RequestBody ApproveProductRequest req) {
        return ApiResponse.ok(sellerService.approveProduct(linkId, req, actor));
    }

    // ─── Payouts ───
    @PostMapping("/admin/payouts")
    public ApiResponse<PayoutResponse> createPayout(@Valid @RequestBody PayoutLineRequest req) {
        return ApiResponse.ok(sellerService.createPayout(req));
    }

    @PostMapping("/admin/payouts/{payoutId}/settled")
    public ApiResponse<PayoutResponse> markSettled(@PathVariable UUID payoutId,
                                                   @RequestParam String externalRef) {
        return ApiResponse.ok(sellerService.markPayoutSettled(payoutId, externalRef));
    }

    @GetMapping("/{sellerId}/payouts")
    public ApiResponse<PageResponse<PayoutResponse>> listPayouts(@PathVariable UUID sellerId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(sellerService.listPayoutsForSeller(sellerId, page, size));
    }
}

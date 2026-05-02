package com.ecoshop.returns.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.returns.service.dto.ReturnsDtos.*;
import com.ecoshop.returns.service.service.ReturnsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnsController {

    private final ReturnsService returnsService;

    // ─── Customer endpoints ───
    @PostMapping
    public ApiResponse<ReturnResponse> create(@AuthenticationPrincipal String userId,
                                              @RequestHeader("Authorization") String authHeader,
                                              @Valid @RequestBody CreateReturnRequest req) {
        return ApiResponse.ok(returnsService.createReturn(UUID.fromString(userId), authHeader, req));
    }

    @GetMapping
    public ApiResponse<PageResponse<ReturnResponse>> listMine(@AuthenticationPrincipal String userId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(returnsService.listMine(UUID.fromString(userId), page, size));
    }

    @GetMapping("/{returnId}")
    public ApiResponse<ReturnResponse> get(@AuthenticationPrincipal String userId,
                                           @PathVariable UUID returnId) {
        return ApiResponse.ok(returnsService.get(UUID.fromString(userId), returnId));
    }

    @GetMapping("/rma/{rmaNumber}")
    public ApiResponse<ReturnResponse> getByRma(@PathVariable String rmaNumber) {
        return ApiResponse.ok(returnsService.getByRma(rmaNumber));
    }

    // ─── Admin / ops endpoints ───
    @PostMapping("/admin/{returnId}/approve")
    public ApiResponse<ReturnResponse> approve(@PathVariable UUID returnId,
                                               @RequestHeader(value = "X-Actor", defaultValue = "admin") String actor,
                                               @RequestBody(required = false) ApproveRequest req) {
        return ApiResponse.ok(returnsService.approve(returnId, req != null ? req : new ApproveRequest(null), actor));
    }

    @PostMapping("/admin/{returnId}/reject")
    public ApiResponse<ReturnResponse> reject(@PathVariable UUID returnId,
                                              @RequestHeader(value = "X-Actor", defaultValue = "admin") String actor,
                                              @Valid @RequestBody RejectRequest req) {
        return ApiResponse.ok(returnsService.reject(returnId, req, actor));
    }

    @PostMapping("/admin/{returnId}/pickup-scheduled")
    public ApiResponse<ReturnResponse> pickupScheduled(@PathVariable UUID returnId,
                                                       @RequestHeader(value = "X-Actor", defaultValue = "ops") String actor) {
        return ApiResponse.ok(returnsService.markPickupScheduled(returnId, actor));
    }

    @PostMapping("/admin/{returnId}/picked-up")
    public ApiResponse<ReturnResponse> pickedUp(@PathVariable UUID returnId,
                                                @RequestHeader(value = "X-Actor", defaultValue = "ops") String actor) {
        return ApiResponse.ok(returnsService.markPickedUp(returnId, actor));
    }

    @PostMapping("/admin/{returnId}/qc")
    public ApiResponse<ReturnResponse> qc(@PathVariable UUID returnId,
                                          @RequestHeader(value = "X-Actor", defaultValue = "ops") String actor,
                                          @Valid @RequestBody QcResultRequest req) {
        return ApiResponse.ok(returnsService.recordQc(returnId, req, actor));
    }

    @PostMapping("/admin/{returnId}/refund")
    public ApiResponse<ReturnResponse> refund(@PathVariable UUID returnId,
                                              @RequestHeader(value = "X-Actor", defaultValue = "ops") String actor,
                                              @RequestHeader("Authorization") String authHeader) {
        return ApiResponse.ok(returnsService.initiateRefund(returnId, actor, authHeader));
    }

    @PostMapping("/admin/{returnId}/close")
    public ApiResponse<ReturnResponse> close(@PathVariable UUID returnId,
                                             @RequestHeader(value = "X-Actor", defaultValue = "ops") String actor) {
        return ApiResponse.ok(returnsService.close(returnId, actor));
    }
}

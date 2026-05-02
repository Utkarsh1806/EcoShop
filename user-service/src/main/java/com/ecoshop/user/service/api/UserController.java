package com.ecoshop.user.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.user.service.dto.UserDtos.*;
import com.ecoshop.user.service.service.AddressService;
import com.ecoshop.user.service.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAccountService userService;
    private final AddressService addressService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(userService.getProfile(UUID.fromString(userId)));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@AuthenticationPrincipal String userId,
                                              @Valid @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(userService.updateProfile(UUID.fromString(userId), req));
    }

    @PostMapping("/me/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal String userId,
                                            @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(UUID.fromString(userId), req);
        return ApiResponse.ok(null);
    }

    // ─── Addresses ───

    @GetMapping("/me/addresses")
    public ApiResponse<List<AddressResponse>> listAddresses(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(addressService.listAddresses(UUID.fromString(userId)));
    }

    @PostMapping("/me/addresses")
    public ApiResponse<AddressResponse> createAddress(@AuthenticationPrincipal String userId,
                                                      @Valid @RequestBody AddressRequest req) {
        return ApiResponse.ok(addressService.createAddress(UUID.fromString(userId), req));
    }

    @PutMapping("/me/addresses/{addressId}")
    public ApiResponse<AddressResponse> updateAddress(@AuthenticationPrincipal String userId,
                                                      @PathVariable UUID addressId,
                                                      @Valid @RequestBody AddressRequest req) {
        return ApiResponse.ok(addressService.updateAddress(UUID.fromString(userId), addressId, req));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ApiResponse<Void> deleteAddress(@AuthenticationPrincipal String userId,
                                           @PathVariable UUID addressId) {
        addressService.deleteAddress(UUID.fromString(userId), addressId);
        return ApiResponse.ok(null);
    }

    // Internal endpoint for other services to fetch user details (Feign callable)
    @GetMapping("/internal/{userId}")
    public ApiResponse<UserResponse> getById(@PathVariable UUID userId) {
        return ApiResponse.ok(userService.getProfile(userId));
    }
}

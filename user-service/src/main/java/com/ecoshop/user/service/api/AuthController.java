package com.ecoshop.user.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.user.service.dto.UserDtos.*;
import com.ecoshop.user.service.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAccountService userService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(userService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(userService.login(req));
    }
}

package com.ecoshop.user.service.dto;

import com.ecoshop.user.service.domain.UserRole;
import com.ecoshop.user.service.domain.UserStatus;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UserDtos {

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            @Size(max = 20) String phone
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            UserResponse user
    ) {}

    public record UserResponse(
            UUID id,
            String email,
            String firstName,
            String lastName,
            String phone,
            UserRole role,
            UserStatus status,
            boolean emailVerified,
            Instant createdAt
    ) {}

    public record UpdateProfileRequest(
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            @Size(max = 20) String phone
    ) {}

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 100) String newPassword
    ) {}

    public record AddressRequest(
            @Size(max = 50) String label,
            @NotBlank @Size(max = 200) String recipientName,
            @NotBlank @Size(max = 20) String phone,
            @NotBlank @Size(max = 255) String line1,
            @Size(max = 255) String line2,
            @NotBlank @Size(max = 100) String city,
            @NotBlank @Size(max = 100) String state,
            @NotBlank @Size(max = 20) String postalCode,
            @Size(min = 2, max = 2) String country,
            boolean isDefault
    ) {}

    public record AddressResponse(
            UUID id,
            String label,
            String recipientName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country,
            boolean isDefault
    ) {}

    public record AddressListResponse(List<AddressResponse> addresses) {}
}

package com.ecoshop.user.service.service;

import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.common.security.JwtService;
import com.ecoshop.user.service.domain.User;
import com.ecoshop.user.service.domain.UserStatus;
import com.ecoshop.user.service.dto.UserDtos.*;
import com.ecoshop.user.service.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${ecoshop.jwt.ttl-seconds:3600}")
    private long jwtTtl;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw BusinessException.conflict("USER_EMAIL_EXISTS",
                    "An account with email " + req.email() + " already exists");
        }
        if (req.phone() != null && !req.phone().isBlank() && userRepository.existsByPhone(req.phone())) {
            throw BusinessException.conflict("USER_PHONE_EXISTS",
                    "An account with this phone already exists");
        }
        User user = User.builder()
                .email(req.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.password()))
                .firstName(req.firstName())
                .lastName(req.lastName())
                .phone(req.phone())
                .status(UserStatus.ACTIVE)
                .build();
        user = userRepository.save(user);
        log.info("New user registered: {}", user.getId());
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> BusinessException.badRequest("INVALID_CREDENTIALS",
                        "Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw BusinessException.badRequest("INVALID_CREDENTIALS", "Invalid email or password");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw BusinessException.badRequest("ACCOUNT_INACTIVE",
                    "Account is " + user.getStatus());
        }
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return toResponse(findUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = findUser(userId);
        if (req.firstName() != null) user.setFirstName(req.firstName());
        if (req.lastName() != null) user.setLastName(req.lastName());
        if (req.phone() != null) user.setPhone(req.phone());
        return toResponse(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest req) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw BusinessException.badRequest("INVALID_PASSWORD", "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        log.info("Password changed for user {}", userId);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("USER_NOT_FOUND",
                        "User " + userId + " not found"));
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = Map.of(
                "userId", user.getId().toString(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        );
        String token = jwtService.issue(user.getId().toString(), claims);
        return new AuthResponse(token, "Bearer", jwtTtl, toResponse(user));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }
}

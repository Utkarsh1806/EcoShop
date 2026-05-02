package com.ecoshop.notification.service.dto;

import com.ecoshop.notification.service.domain.Channel;
import com.ecoshop.notification.service.domain.NotificationStatus;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public class NotificationDtos {

    public record SendRequest(
            UUID userId,
            @NotNull Channel channel,
            @NotBlank String recipient,
            String subject,
            @NotBlank String body,
            String templateKey,
            @NotBlank String dedupeKey
    ) {}

    public record NotificationResponse(
            UUID id, UUID userId, Channel channel, String recipient,
            String subject, String body, String templateKey,
            NotificationStatus status, Instant sentAt, String failureReason, int retryCount
    ) {}
}

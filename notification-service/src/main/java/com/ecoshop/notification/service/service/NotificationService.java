package com.ecoshop.notification.service.service;

import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.notification.service.channel.ChannelSender;
import com.ecoshop.notification.service.domain.Channel;
import com.ecoshop.notification.service.domain.Notification;
import com.ecoshop.notification.service.domain.NotificationStatus;
import com.ecoshop.notification.service.dto.NotificationDtos.*;
import com.ecoshop.notification.service.repo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final Map<Channel, ChannelSender> sendersByChannel;

    @Autowired
    public NotificationService(NotificationRepository repository, List<ChannelSender> senders) {
        this.repository = repository;
        this.sendersByChannel = new EnumMap<>(Channel.class);
        for (ChannelSender s : senders) {
            this.sendersByChannel.put(s.channel(), s);
        }
        log.info("NotificationService initialized with channels: {}", sendersByChannel.keySet());
    }

    /**
     * Idempotent send. Re-using the same dedupeKey returns the existing notification
     * without re-sending. Useful when consuming Kafka events at-least-once.
     */
    @Transactional
    public NotificationResponse send(SendRequest req) {
        return repository.findByDedupeKey(req.dedupeKey())
                .map(this::toResponse)
                .orElseGet(() -> doSend(req));
    }

    private NotificationResponse doSend(SendRequest req) {
        Notification n = Notification.builder()
                .userId(req.userId())
                .channel(req.channel())
                .recipient(req.recipient())
                .subject(req.subject())
                .body(req.body())
                .templateKey(req.templateKey())
                .dedupeKey(req.dedupeKey())
                .status(NotificationStatus.QUEUED)
                .build();
        n = repository.save(n);

        ChannelSender sender = sendersByChannel.get(req.channel());
        if (sender == null) {
            n.setStatus(NotificationStatus.FAILED);
            n.setFailureReason("No sender registered for channel " + req.channel());
            return toResponse(n);
        }
        try {
            n.setStatus(NotificationStatus.SENDING);
            String providerId = sender.send(n);
            n.setStatus(NotificationStatus.SENT);
            n.setSentAt(Instant.now());
            log.info("Sent {} notification {} via {} (provider id: {})",
                    req.channel(), n.getId(), sender.getClass().getSimpleName(), providerId);
        } catch (Exception e) {
            n.setStatus(NotificationStatus.FAILED);
            n.setFailureReason(e.getMessage());
            n.setRetryCount(n.getRetryCount() + 1);
            log.error("Failed to send notification {}: {}", n.getId(), e.getMessage());
        }
        return toResponse(n);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID id) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("NOTIFICATION_NOT_FOUND", "Not found"));
        return toResponse(n);
    }

    public NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getUserId(), n.getChannel(), n.getRecipient(),
                n.getSubject(), n.getBody(), n.getTemplateKey(),
                n.getStatus(), n.getSentAt(), n.getFailureReason(), n.getRetryCount()
        );
    }
}

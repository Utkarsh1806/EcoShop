package com.ecoshop.notification.service.repo;

import com.ecoshop.notification.service.domain.Notification;
import com.ecoshop.notification.service.domain.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByDedupeKey(String dedupeKey);
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<Notification> findTop50ByStatusOrderByCreatedAtAsc(NotificationStatus status);
}

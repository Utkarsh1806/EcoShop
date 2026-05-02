package com.ecoshop.inventory.service.repo;

import com.ecoshop.inventory.service.domain.Reservation;
import com.ecoshop.inventory.service.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Optional<Reservation> findByReferenceId(String referenceId);
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant cutoff);
}

package com.ecoshop.common.event;

import java.time.Instant;
import java.util.UUID;

/** Marker for all events published to Kafka. Concrete events should be records. */
public interface DomainEvent {
    UUID eventId();
    Instant occurredAt();
    String aggregateType();
    String aggregateId();
}

package com.taskmanager.domain.events;

import java.time.LocalDateTime;

/**
 * Marker for domain events.
 */
public interface DomainEvent {
    LocalDateTime occurredAt();
}

package com.taskmanager.domain.events;

import com.taskmanager.domain.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event emitted when a task status changes.
 */
public record TaskStatusChangedEvent(
        UUID taskId,
        TaskStatus previousStatus,
        TaskStatus newStatus,
        UUID changedBy,
        LocalDateTime occurredAt
) implements DomainEvent {
}

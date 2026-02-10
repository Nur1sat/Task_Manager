package com.taskmanager.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable status history entry for a task.
 */
public final class TaskStatusHistory {
    private final UUID id;
    private final UUID taskId;
    private final TaskStatus previousStatus;
    private final TaskStatus newStatus;
    private final LocalDateTime changedAt;
    private final UUID changedBy;

    public TaskStatusHistory(UUID id,
                             UUID taskId,
                             TaskStatus previousStatus,
                             TaskStatus newStatus,
                             LocalDateTime changedAt,
                             UUID changedBy) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.taskId = Objects.requireNonNull(taskId, "taskId is required");
        this.previousStatus = Objects.requireNonNull(previousStatus, "previousStatus is required");
        this.newStatus = Objects.requireNonNull(newStatus, "newStatus is required");
        this.changedAt = Objects.requireNonNull(changedAt, "changedAt is required");
        this.changedBy = changedBy;
    }

    public static TaskStatusHistory create(UUID taskId,
                                           TaskStatus previousStatus,
                                           TaskStatus newStatus,
                                           LocalDateTime changedAt,
                                           UUID changedBy) {
        return new TaskStatusHistory(UUID.randomUUID(), taskId, previousStatus, newStatus, changedAt, changedBy);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public TaskStatus getPreviousStatus() {
        return previousStatus;
    }

    public TaskStatus getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public UUID getChangedBy() {
        return changedBy;
    }
}

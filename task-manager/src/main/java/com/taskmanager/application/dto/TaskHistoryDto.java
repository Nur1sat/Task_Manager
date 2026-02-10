package com.taskmanager.application.dto;

import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.domain.model.TaskStatusHistory;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Status transition read model for task history.
 */
public record TaskHistoryDto(
        UUID id,
        UUID taskId,
        TaskStatus previousStatus,
        TaskStatus newStatus,
        LocalDateTime changedAt,
        UUID changedBy
) {
    public static TaskHistoryDto fromDomain(TaskStatusHistory history) {
        return new TaskHistoryDto(
                history.getId(),
                history.getTaskId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedAt(),
                history.getChangedBy()
        );
    }
}

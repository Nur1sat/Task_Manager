package com.taskmanager.application.dto;

import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Read model used by input ports and UI adapters.
 */
public record TaskDto(
        UUID id,
        UUID projectId,
        String title,
        String description,
        Priority priority,
        TaskStatus status,
        LocalDate dueDate,
        UUID assigneeId,
        Set<String> tags,
        Set<UUID> blockingTaskIds,
        boolean archived,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt
) {
    public static TaskDto fromDomain(Task task) {
        return new TaskDto(
                task.getId(),
                task.getProjectId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDueDate(),
                task.getAssigneeId(),
                task.getTags(),
                task.getBlockingTaskIds(),
                task.isArchived(),
                task.getAuditInfo().getCreatedAt(),
                task.getAuditInfo().getUpdatedAt(),
                task.getCompletedAt()
        );
    }
}

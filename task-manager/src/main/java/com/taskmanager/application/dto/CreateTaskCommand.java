package com.taskmanager.application.dto;

import com.taskmanager.domain.model.Priority;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Input command for creating a task.
 */
public record CreateTaskCommand(
        UUID projectId,
        String title,
        String description,
        Priority priority,
        LocalDate dueDate,
        UUID assigneeId,
        Set<String> tags,
        Set<UUID> blockingTaskIds
) {
}

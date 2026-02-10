package com.taskmanager.application.dto;

import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.TaskStatus;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Input command for updating task data.
 */
public record UpdateTaskCommand(
        UUID taskId,
        String title,
        String description,
        Priority priority,
        TaskStatus status,
        LocalDate dueDate,
        Set<String> tags,
        Set<UUID> blockingTaskIds,
        boolean archive
) {
}

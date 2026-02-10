package com.taskmanager.application.dto;

import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.TaskStatus;

import java.util.UUID;

/**
 * Query criteria for searching tasks.
 */
public record SearchTasksQuery(
        UUID projectId,
        TaskStatus status,
        Priority priority,
        UUID assigneeId,
        boolean includeArchived
) {
}

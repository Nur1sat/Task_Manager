package com.taskmanager.app.ui.viewmodels;

import com.taskmanager.domain.model.Priority;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Form payload sent from UI to task creation logic.
 */
public record NewTaskForm(
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

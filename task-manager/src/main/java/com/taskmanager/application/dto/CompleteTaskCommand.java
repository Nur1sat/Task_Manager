package com.taskmanager.application.dto;

import java.util.UUID;

/**
 * Input command for completing a task.
 */
public record CompleteTaskCommand(UUID taskId, UUID completedByUserId) {
}

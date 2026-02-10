package com.taskmanager.application.dto;

import java.util.UUID;

/**
 * Input command for assigning a task to a user.
 */
public record AssignTaskCommand(UUID taskId, UUID assigneeId) {
}

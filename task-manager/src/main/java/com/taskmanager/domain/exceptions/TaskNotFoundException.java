package com.taskmanager.domain.exceptions;

import java.util.UUID;

/**
 * Thrown when a task cannot be found.
 */
public class TaskNotFoundException extends DomainException {
    public TaskNotFoundException(UUID taskId) {
        super("Task not found: " + taskId);
    }
}

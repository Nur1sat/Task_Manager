package com.taskmanager.domain.valueobjects;

import java.util.UUID;

/**
 * Task identifier value object.
 */
public record TaskId(UUID value) {
    public TaskId {
        if (value == null) {
            throw new IllegalArgumentException("TaskId value is required");
        }
    }

    public static TaskId random() {
        return new TaskId(UUID.randomUUID());
    }
}

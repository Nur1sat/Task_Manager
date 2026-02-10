package com.taskmanager.domain.valueobjects;

import java.util.UUID;

/**
 * Project identifier value object.
 */
public record ProjectId(UUID value) {
    public ProjectId {
        if (value == null) {
            throw new IllegalArgumentException("ProjectId value is required");
        }
    }

    public static ProjectId random() {
        return new ProjectId(UUID.randomUUID());
    }
}

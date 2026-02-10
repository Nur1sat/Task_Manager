package com.taskmanager.domain.valueobjects;

import java.util.UUID;

/**
 * User identifier value object.
 */
public record UserId(UUID value) {
    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId value is required");
        }
    }

    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }
}

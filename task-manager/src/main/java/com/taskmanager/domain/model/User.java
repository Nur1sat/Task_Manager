package com.taskmanager.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable user aggregate root.
 */
public final class User {
    private final UUID id;
    private final String username;
    private final String email;
    private final boolean active;

    public User(UUID id, String username, String email, boolean active) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.username = requireText(username, "username");
        this.email = requireText(email, "email");
        this.active = active;
    }

    public static User create(String username, String email) {
        return new User(UUID.randomUUID(), username, email, true);
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public User deactivate() {
        return new User(id, username, email, false);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}

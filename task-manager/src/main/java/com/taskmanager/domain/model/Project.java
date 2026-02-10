package com.taskmanager.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable project aggregate root.
 */
public final class Project {
    private final UUID id;
    private final String name;
    private final String description;
    private final UUID ownerUserId;
    private final boolean archived;

    public Project(UUID id, String name, String description, UUID ownerUserId, boolean archived) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.name = requireText(name, "name");
        this.description = description == null ? "" : description.trim();
        this.ownerUserId = Objects.requireNonNull(ownerUserId, "ownerUserId is required");
        this.archived = archived;
    }

    public static Project create(String name, String description, UUID ownerUserId) {
        return new Project(UUID.randomUUID(), name, description, ownerUserId, false);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public boolean isArchived() {
        return archived;
    }

    public Project archive() {
        return new Project(id, name, description, ownerUserId, true);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}

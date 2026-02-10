package com.taskmanager.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable audit metadata.
 */
public final class AuditInfo {
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AuditInfo(LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt cannot be before createdAt");
        }
    }

    public static AuditInfo createdNow(LocalDateTime now) {
        return new AuditInfo(now, now);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public AuditInfo touch(LocalDateTime updatedAt) {
        return new AuditInfo(this.createdAt, Objects.requireNonNull(updatedAt, "updatedAt is required"));
    }
}

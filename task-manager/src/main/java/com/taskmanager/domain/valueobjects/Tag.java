package com.taskmanager.domain.valueobjects;

/**
 * Canonical tag value object.
 */
public record Tag(String value) {
    public Tag(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tag value is required");
        }
        this.value = value.trim().toLowerCase();
    }
}

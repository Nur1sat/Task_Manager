package com.taskmanager.domain.model;

/**
 * Lifecycle statuses for tasks.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    BLOCKED,
    COMPLETED,
    ARCHIVED;

    /**
     * @return true when the status is terminal.
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == ARCHIVED;
    }
}

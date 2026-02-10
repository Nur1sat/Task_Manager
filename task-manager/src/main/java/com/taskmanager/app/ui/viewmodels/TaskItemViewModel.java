package com.taskmanager.app.ui.viewmodels;

import com.taskmanager.application.dto.TaskDto;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable presentation model for a task row in list views.
 */
public final class TaskItemViewModel {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TaskDto source;
    private final String assigneeName;

    public TaskItemViewModel(TaskDto source, String assigneeName) {
        this.source = Objects.requireNonNull(source, "source is required");
        this.assigneeName = assigneeName == null ? "Unassigned" : assigneeName;
    }

    public UUID id() {
        return source.id();
    }

    public UUID projectId() {
        return source.projectId();
    }

    public String title() {
        return source.title();
    }

    public String description() {
        return source.description();
    }

    public String status() {
        return source.status().name();
    }

    public String priority() {
        return source.priority().name();
    }

    public String dueDate() {
        return source.dueDate().format(DATE_FORMAT);
    }

    public String tags() {
        return source.tags().isEmpty() ? "-" : String.join(", ", source.tags());
    }

    public String assigneeName() {
        return assigneeName;
    }

    public TaskDto source() {
        return source;
    }

    @Override
    public String toString() {
        return "%s | %s | %s | due %s".formatted(source.title(), source.status(), source.priority(), dueDate());
    }
}

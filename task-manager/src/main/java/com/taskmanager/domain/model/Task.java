package com.taskmanager.domain.model;

import com.taskmanager.domain.exceptions.InvalidTaskStateException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable task aggregate root.
 */
public final class Task {
    private final UUID id;
    private final UUID projectId;
    private final String title;
    private final String description;
    private final Priority priority;
    private final TaskStatus status;
    private final LocalDate dueDate;
    private final UUID assigneeId;
    private final Set<String> tags;
    private final Set<UUID> blockingTaskIds;
    private final boolean archived;
    private final AuditInfo auditInfo;
    private final LocalDateTime completedAt;

    public Task(UUID id,
                UUID projectId,
                String title,
                String description,
                Priority priority,
                TaskStatus status,
                LocalDate dueDate,
                UUID assigneeId,
                Set<String> tags,
                Set<UUID> blockingTaskIds,
                boolean archived,
                AuditInfo auditInfo,
                LocalDateTime completedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.projectId = Objects.requireNonNull(projectId, "projectId is required");
        this.title = requireText(title, "title");
        this.description = description == null ? "" : description.trim();
        this.priority = Objects.requireNonNull(priority, "priority is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate is required");
        this.assigneeId = assigneeId;
        this.tags = sanitizeTags(tags);
        this.blockingTaskIds = sanitizeBlockingTaskIds(blockingTaskIds, id);
        this.archived = archived;
        this.auditInfo = Objects.requireNonNull(auditInfo, "auditInfo is required");
        this.completedAt = completedAt;

        if (status == TaskStatus.COMPLETED && completedAt == null) {
            throw new IllegalArgumentException("completedAt is required for completed tasks");
        }
        if (status == TaskStatus.ARCHIVED && !archived) {
            throw new IllegalArgumentException("archived must be true when status is ARCHIVED");
        }
    }

    /**
     * Factory for creating a new task.
     */
    public static Task create(UUID projectId,
                              String title,
                              String description,
                              Priority priority,
                              LocalDate dueDate,
                              UUID assigneeId,
                              Set<String> tags,
                              Set<UUID> blockingTaskIds,
                              LocalDateTime now) {
        AuditInfo auditInfo = AuditInfo.createdNow(Objects.requireNonNull(now, "now is required"));
        return new Task(
                UUID.randomUUID(),
                projectId,
                title,
                description,
                priority == null ? Priority.MEDIUM : priority,
                TaskStatus.TODO,
                dueDate,
                assigneeId,
                tags,
                blockingTaskIds,
                false,
                auditInfo,
                null
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Set<UUID> getBlockingTaskIds() {
        return blockingTaskIds;
    }

    public boolean isArchived() {
        return archived;
    }

    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * Returns a modified copy with updated fields.
     */
    public Task update(String title,
                       String description,
                       Priority priority,
                       LocalDate dueDate,
                       Set<String> tags,
                       Set<UUID> blockingTaskIds,
                       LocalDateTime updatedAt) {
        ensureNotArchived();
        return new Task(
                id,
                projectId,
                title == null ? this.title : title,
                description == null ? this.description : description,
                priority == null ? this.priority : priority,
                status,
                dueDate == null ? this.dueDate : dueDate,
                assigneeId,
                tags == null ? this.tags : tags,
                blockingTaskIds == null ? this.blockingTaskIds : blockingTaskIds,
                archived,
                auditInfo.touch(Objects.requireNonNull(updatedAt, "updatedAt is required")),
                completedAt
        );
    }

    /**
     * Returns a modified copy with an assignee.
     */
    public Task assign(UUID assigneeId, LocalDateTime updatedAt) {
        ensureNotArchived();
        return new Task(
                id,
                projectId,
                title,
                description,
                priority,
                status,
                dueDate,
                assigneeId,
                tags,
                blockingTaskIds,
                archived,
                auditInfo.touch(Objects.requireNonNull(updatedAt, "updatedAt is required")),
                completedAt
        );
    }

    /**
     * Returns a modified copy with a different status.
     */
    public Task changeStatus(TaskStatus newStatus, LocalDateTime updatedAt) {
        Objects.requireNonNull(newStatus, "newStatus is required");
        ensureValidTransition(this.status, newStatus);
        return new Task(
                id,
                projectId,
                title,
                description,
                priority,
                newStatus,
                dueDate,
                assigneeId,
                tags,
                blockingTaskIds,
                newStatus == TaskStatus.ARCHIVED || archived,
                auditInfo.touch(Objects.requireNonNull(updatedAt, "updatedAt is required")),
                newStatus == TaskStatus.COMPLETED ? updatedAt : completedAt
        );
    }

    /**
     * Marks this task complete.
     */
    public Task complete(LocalDateTime updatedAt) {
        return changeStatus(TaskStatus.COMPLETED, updatedAt);
    }

    /**
     * Soft deletes this task by archiving it.
     */
    public Task archive(LocalDateTime updatedAt) {
        if (status == TaskStatus.ARCHIVED) {
            return this;
        }
        return new Task(
                id,
                projectId,
                title,
                description,
                priority,
                TaskStatus.ARCHIVED,
                dueDate,
                assigneeId,
                tags,
                blockingTaskIds,
                true,
                auditInfo.touch(Objects.requireNonNull(updatedAt, "updatedAt is required")),
                completedAt
        );
    }

    /**
     * @return true when due date is before the given date and task is not completed.
     */
    public boolean isOverdue(LocalDate onDate) {
        LocalDate reference = onDate == null ? LocalDate.now() : onDate;
        return !status.isTerminal() && dueDate.isBefore(reference);
    }

    /**
     * @return completion age in days, if completed.
     */
    public long completionDurationDays() {
        if (completedAt == null) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(auditInfo.getCreatedAt(), completedAt);
    }

    private void ensureNotArchived() {
        if (archived || status == TaskStatus.ARCHIVED) {
            throw new InvalidTaskStateException("Archived tasks cannot be modified");
        }
    }

    private static void ensureValidTransition(TaskStatus current, TaskStatus target) {
        if (current == target) {
            return;
        }
        if (current == TaskStatus.ARCHIVED) {
            throw new InvalidTaskStateException("Cannot transition from ARCHIVED");
        }
        if (current == TaskStatus.COMPLETED && target != TaskStatus.ARCHIVED) {
            throw new InvalidTaskStateException("Completed tasks can only be archived");
        }
        if (target == TaskStatus.TODO && current == TaskStatus.COMPLETED) {
            throw new InvalidTaskStateException("Cannot reopen completed task to TODO");
        }
    }

    private static Set<String> sanitizeTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> clean = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                clean.add(tag.trim().toLowerCase());
            }
        }
        return Collections.unmodifiableSet(clean);
    }

    private static Set<UUID> sanitizeBlockingTaskIds(Set<UUID> blockingTaskIds, UUID taskId) {
        if (blockingTaskIds == null || blockingTaskIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<UUID> clean = new LinkedHashSet<>();
        for (UUID id : blockingTaskIds) {
            if (id != null && !id.equals(taskId)) {
                clean.add(id);
            }
        }
        return Collections.unmodifiableSet(clean);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}

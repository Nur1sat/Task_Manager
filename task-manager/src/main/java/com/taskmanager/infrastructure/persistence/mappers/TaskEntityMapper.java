package com.taskmanager.infrastructure.persistence.mappers;

import com.taskmanager.domain.model.AuditInfo;
import com.taskmanager.domain.model.Task;
import com.taskmanager.infrastructure.persistence.entities.TaskEntity;

import java.util.LinkedHashSet;

/**
 * Maps between {@link Task} and {@link TaskEntity}.
 */
public class TaskEntityMapper {
    public TaskEntity toEntity(Task task) {
        TaskEntity entity = new TaskEntity();
        entity.setId(task.getId());
        entity.setProjectId(task.getProjectId());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setPriority(task.getPriority());
        entity.setStatus(task.getStatus());
        entity.setDueDate(task.getDueDate());
        entity.setAssigneeId(task.getAssigneeId());
        entity.setTags(new LinkedHashSet<>(task.getTags()));
        entity.setBlockingTaskIds(new LinkedHashSet<>(task.getBlockingTaskIds()));
        entity.setArchived(task.isArchived());
        entity.setCreatedAt(task.getAuditInfo().getCreatedAt());
        entity.setUpdatedAt(task.getAuditInfo().getUpdatedAt());
        entity.setCompletedAt(task.getCompletedAt());
        return entity;
    }

    public Task toDomain(TaskEntity entity) {
        return new Task(
                entity.getId(),
                entity.getProjectId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getDueDate(),
                entity.getAssigneeId(),
                entity.getTags(),
                entity.getBlockingTaskIds(),
                entity.isArchived(),
                new AuditInfo(entity.getCreatedAt(), entity.getUpdatedAt()),
                entity.getCompletedAt()
        );
    }
}

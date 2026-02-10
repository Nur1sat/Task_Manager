package com.taskmanager.infrastructure.persistence.mappers;

import com.taskmanager.domain.model.TaskStatusHistory;
import com.taskmanager.infrastructure.persistence.entities.TaskHistoryEntity;

/**
 * Maps between {@link TaskStatusHistory} and {@link TaskHistoryEntity}.
 */
public class TaskHistoryEntityMapper {
    public TaskHistoryEntity toEntity(TaskStatusHistory history) {
        TaskHistoryEntity entity = new TaskHistoryEntity();
        entity.setId(history.getId());
        entity.setTaskId(history.getTaskId());
        entity.setPreviousStatus(history.getPreviousStatus());
        entity.setNewStatus(history.getNewStatus());
        entity.setChangedAt(history.getChangedAt());
        entity.setChangedBy(history.getChangedBy());
        return entity;
    }

    public TaskStatusHistory toDomain(TaskHistoryEntity entity) {
        return new TaskStatusHistory(
                entity.getId(),
                entity.getTaskId(),
                entity.getPreviousStatus(),
                entity.getNewStatus(),
                entity.getChangedAt(),
                entity.getChangedBy()
        );
    }
}

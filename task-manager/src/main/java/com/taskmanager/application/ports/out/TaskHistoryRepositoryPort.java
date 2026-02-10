package com.taskmanager.application.ports.out;

import com.taskmanager.domain.model.TaskStatusHistory;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for task status history persistence.
 */
public interface TaskHistoryRepositoryPort {
    TaskStatusHistory save(TaskStatusHistory history);

    List<TaskStatusHistory> findByTaskId(UUID taskId);
}

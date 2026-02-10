package com.taskmanager.application.usecases;

import com.taskmanager.application.dto.AssignTaskCommand;
import com.taskmanager.application.dto.TaskDto;
import com.taskmanager.application.ports.in.AssignTaskInputPort;
import com.taskmanager.application.ports.out.TaskHistoryRepositoryPort;
import com.taskmanager.application.ports.out.TaskRepositoryPort;
import com.taskmanager.application.ports.out.UserRepositoryPort;
import com.taskmanager.domain.exceptions.TaskNotFoundException;
import com.taskmanager.domain.exceptions.TaskValidationException;
import com.taskmanager.domain.exceptions.UserNotFoundException;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.domain.model.TaskStatusHistory;
import com.taskmanager.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Application service for assigning tasks.
 */
public class AssignTaskUseCase implements AssignTaskInputPort {
    private static final Logger logger = LoggerFactory.getLogger(AssignTaskUseCase.class);

    private final TaskRepositoryPort taskRepository;
    private final UserRepositoryPort userRepository;
    private final TaskHistoryRepositoryPort historyRepository;
    private final Clock clock;

    public AssignTaskUseCase(TaskRepositoryPort taskRepository,
                             UserRepositoryPort userRepository,
                             TaskHistoryRepositoryPort historyRepository,
                             Clock clock) {
        this.taskRepository = Objects.requireNonNull(taskRepository, "taskRepository is required");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository is required");
        this.historyRepository = Objects.requireNonNull(historyRepository, "historyRepository is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    @Override
    public TaskDto execute(AssignTaskCommand command) {
        validate(command);

        Task task = taskRepository.findById(command.taskId())
                .orElseThrow(() -> new TaskNotFoundException(command.taskId()));

        User assignee = userRepository.findById(command.assigneeId())
                .orElseThrow(() -> new UserNotFoundException(command.assigneeId()));

        if (!assignee.isActive()) {
            throw new TaskValidationException("Assignee is not active: " + assignee.getId());
        }

        LocalDateTime now = LocalDateTime.now(clock);
        TaskStatus previousStatus = task.getStatus();
        Task assigned = task.assign(assignee.getId(), now);

        if (assigned.getStatus() == TaskStatus.TODO) {
            assigned = assigned.changeStatus(TaskStatus.IN_PROGRESS, now);
            TaskStatusHistory history = TaskStatusHistory.create(
                    assigned.getId(),
                    previousStatus,
                    TaskStatus.IN_PROGRESS,
                    now,
                    assignee.getId()
            );
            historyRepository.save(history);
        }

        Task saved = taskRepository.save(assigned);
        logger.info("Task {} assigned to {}", saved.getId(), assignee.getId());
        return TaskDto.fromDomain(saved);
    }

    private void validate(AssignTaskCommand command) {
        if (command == null || command.taskId() == null || command.assigneeId() == null) {
            throw new TaskValidationException("taskId and assigneeId are required");
        }
    }
}

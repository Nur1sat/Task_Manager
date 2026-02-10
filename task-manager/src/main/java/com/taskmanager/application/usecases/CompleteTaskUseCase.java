package com.taskmanager.application.usecases;

import com.taskmanager.application.dto.CompleteTaskCommand;
import com.taskmanager.application.dto.TaskDto;
import com.taskmanager.application.ports.in.CompleteTaskInputPort;
import com.taskmanager.application.ports.out.TaskHistoryRepositoryPort;
import com.taskmanager.application.ports.out.TaskRepositoryPort;
import com.taskmanager.application.ports.out.UserRepositoryPort;
import com.taskmanager.domain.exceptions.InvalidTaskStateException;
import com.taskmanager.domain.exceptions.TaskNotFoundException;
import com.taskmanager.domain.exceptions.TaskValidationException;
import com.taskmanager.domain.exceptions.UserNotFoundException;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.domain.model.TaskStatusHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service for task completion.
 */
public class CompleteTaskUseCase implements CompleteTaskInputPort {
    private static final Logger logger = LoggerFactory.getLogger(CompleteTaskUseCase.class);

    private final TaskRepositoryPort taskRepository;
    private final UserRepositoryPort userRepository;
    private final TaskHistoryRepositoryPort historyRepository;
    private final Clock clock;

    public CompleteTaskUseCase(TaskRepositoryPort taskRepository,
                               UserRepositoryPort userRepository,
                               TaskHistoryRepositoryPort historyRepository,
                               Clock clock) {
        this.taskRepository = Objects.requireNonNull(taskRepository, "taskRepository is required");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository is required");
        this.historyRepository = Objects.requireNonNull(historyRepository, "historyRepository is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    @Override
    public TaskDto execute(CompleteTaskCommand command) {
        validate(command);

        Task task = taskRepository.findById(command.taskId())
                .orElseThrow(() -> new TaskNotFoundException(command.taskId()));

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new InvalidTaskStateException("Task is already completed");
        }
        if (task.getStatus() == TaskStatus.ARCHIVED) {
            throw new InvalidTaskStateException("Archived task cannot be completed");
        }

        if (command.completedByUserId() != null) {
            userRepository.findById(command.completedByUserId())
                    .orElseThrow(() -> new UserNotFoundException(command.completedByUserId()));
        }

        ensureDependenciesSatisfied(task);

        LocalDateTime now = LocalDateTime.now(clock);
        TaskStatus previousStatus = task.getStatus();

        Task completed = task.complete(now);
        Task saved = taskRepository.save(completed);

        TaskStatusHistory history = TaskStatusHistory.create(
                saved.getId(),
                previousStatus,
                TaskStatus.COMPLETED,
                now,
                command.completedByUserId()
        );
        historyRepository.save(history);

        logger.info("Task completed: {}", saved.getId());
        return TaskDto.fromDomain(saved);
    }

    private void ensureDependenciesSatisfied(Task task) {
        for (UUID blockerId : task.getBlockingTaskIds()) {
            Task blocker = taskRepository.findById(blockerId)
                    .orElseThrow(() -> new InvalidTaskStateException("Blocking task not found: " + blockerId));
            if (blocker.getStatus() != TaskStatus.COMPLETED) {
                throw new InvalidTaskStateException("Blocking task is not completed: " + blockerId);
            }
        }
    }

    private void validate(CompleteTaskCommand command) {
        if (command == null || command.taskId() == null) {
            throw new TaskValidationException("taskId is required");
        }
    }
}

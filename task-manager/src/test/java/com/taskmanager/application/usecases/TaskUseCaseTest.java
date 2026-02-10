package com.taskmanager.application.usecases;

import com.taskmanager.application.dto.CompleteTaskCommand;
import com.taskmanager.application.dto.CreateTaskCommand;
import com.taskmanager.application.dto.SearchTasksQuery;
import com.taskmanager.application.ports.out.ProjectRepositoryPort;
import com.taskmanager.application.ports.out.TaskHistoryRepositoryPort;
import com.taskmanager.application.ports.out.TaskRepositoryPort;
import com.taskmanager.application.ports.out.UserRepositoryPort;
import com.taskmanager.domain.exceptions.InvalidTaskStateException;
import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.Project;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.domain.model.TaskStatusHistory;
import com.taskmanager.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Basic tests for core task application services.
 */
class TaskUseCaseTest {
    private InMemoryTaskRepository taskRepository;
    private InMemoryProjectRepository projectRepository;
    private InMemoryUserRepository userRepository;
    private InMemoryTaskHistoryRepository historyRepository;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        taskRepository = new InMemoryTaskRepository();
        projectRepository = new InMemoryProjectRepository();
        userRepository = new InMemoryUserRepository();
        historyRepository = new InMemoryTaskHistoryRepository();
        fixedClock = Clock.fixed(Instant.parse("2025-01-10T09:00:00Z"), ZoneOffset.UTC);
    }

    @Test
    void createTaskPersistsTask() {
        User owner = userRepository.save(User.create("owner", "owner@example.com"));
        Project project = projectRepository.save(Project.create("Core", "Core backlog", owner.getId()));

        CreateTaskUseCase useCase = new CreateTaskUseCase(taskRepository, projectRepository, userRepository, fixedClock);

        var created = useCase.execute(new CreateTaskCommand(
                project.getId(),
                "Build reporting",
                "Implement analytics section",
                Priority.HIGH,
                LocalDate.now().plusDays(3),
                owner.getId(),
                java.util.Set.of("analytics", "dashboard"),
                java.util.Set.of()
        ));

        assertNotNull(created.id());
        assertEquals("Build reporting", created.title());
        assertEquals(1, taskRepository.tasks.size());
        assertEquals(TaskStatus.TODO, taskRepository.tasks.get(created.id()).getStatus());
    }

    @Test
    void completeTaskFailsWhenBlockingTaskIsIncomplete() {
        User owner = userRepository.save(User.create("owner", "owner@example.com"));
        Project project = projectRepository.save(Project.create("Core", "Core backlog", owner.getId()));

        Task blocker = taskRepository.save(Task.create(
                project.getId(),
                "Blocker",
                "Must finish first",
                Priority.HIGH,
                LocalDate.now().plusDays(1),
                owner.getId(),
                java.util.Set.of(),
                java.util.Set.of(),
                LocalDateTime.now(fixedClock)
        ));

        Task dependent = taskRepository.save(Task.create(
                project.getId(),
                "Dependent",
                "Depends on blocker",
                Priority.MEDIUM,
                LocalDate.now().plusDays(2),
                owner.getId(),
                java.util.Set.of(),
                java.util.Set.of(blocker.getId()),
                LocalDateTime.now(fixedClock)
        ));

        CompleteTaskUseCase useCase = new CompleteTaskUseCase(taskRepository, userRepository, historyRepository, fixedClock);

        assertThrows(InvalidTaskStateException.class,
                () -> useCase.execute(new CompleteTaskCommand(dependent.getId(), owner.getId())));
    }

    @Test
    void analyticsUseCaseReturnsAggregatedValues() {
        TaskAnalyticsUseCase useCase = new TaskAnalyticsUseCase(taskRepository);

        User owner = userRepository.save(User.create("owner", "owner@example.com"));
        Project project = projectRepository.save(Project.create("Core", "Core backlog", owner.getId()));

        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 8, 0);
        Task completed = Task.create(
                project.getId(),
                "Completed Task",
                "Done",
                Priority.LOW,
                LocalDate.of(2025, 1, 2),
                null,
                java.util.Set.of(),
                java.util.Set.of(),
                createdAt
        ).complete(LocalDateTime.of(2025, 1, 2, 8, 0));

        Task overdue = Task.create(
                project.getId(),
                "Overdue Task",
                "Late",
                Priority.HIGH,
                LocalDate.of(2024, 12, 31),
                null,
                java.util.Set.of(),
                java.util.Set.of(),
                createdAt
        );

        taskRepository.save(completed);
        taskRepository.save(overdue);

        var report = useCase.execute(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 3));

        assertEquals(1L, report.completedPerDay().get(LocalDate.of(2025, 1, 2)));
        assertEquals(1L, report.overdueTasks());
        assertEquals(1.0, report.averageCompletionTimeDays());
    }

    private static final class InMemoryTaskRepository implements TaskRepositoryPort {
        private final Map<UUID, Task> tasks = new LinkedHashMap<>();

        @Override
        public Task save(Task task) {
            tasks.put(task.getId(), task);
            return task;
        }

        @Override
        public Optional<Task> findById(UUID taskId) {
            return Optional.ofNullable(tasks.get(taskId));
        }

        @Override
        public List<Task> findAll(boolean includeArchived) {
            return tasks.values().stream()
                    .filter(task -> includeArchived || !task.isArchived())
                    .toList();
        }

        @Override
        public List<Task> findByProjectId(UUID projectId, boolean includeArchived) {
            return tasks.values().stream()
                    .filter(task -> task.getProjectId().equals(projectId))
                    .filter(task -> includeArchived || !task.isArchived())
                    .toList();
        }

        @Override
        public List<Task> search(SearchTasksQuery query) {
            return tasks.values().stream()
                    .filter(task -> query.projectId() == null || task.getProjectId().equals(query.projectId()))
                    .filter(task -> query.status() == null || task.getStatus() == query.status())
                    .filter(task -> query.priority() == null || task.getPriority() == query.priority())
                    .filter(task -> query.assigneeId() == null || query.assigneeId().equals(task.getAssigneeId()))
                    .filter(task -> query.includeArchived() || !task.isArchived())
                    .toList();
        }

        @Override
        public Map<LocalDate, Long> countCompletedPerDay(LocalDate from, LocalDate to) {
            Map<LocalDate, Long> result = new LinkedHashMap<>();
            LocalDate cursor = from;
            while (!cursor.isAfter(to)) {
                result.put(cursor, 0L);
                cursor = cursor.plusDays(1);
            }

            for (Task task : tasks.values()) {
                if (task.getCompletedAt() == null) {
                    continue;
                }
                LocalDate completionDate = task.getCompletedAt().toLocalDate();
                if (!completionDate.isBefore(from) && !completionDate.isAfter(to)) {
                    result.computeIfPresent(completionDate, (k, v) -> v + 1);
                }
            }
            return result;
        }

        @Override
        public long countOverdue(LocalDate onDate) {
            return tasks.values().stream().filter(task -> task.isOverdue(onDate)).count();
        }

        @Override
        public double averageCompletionTimeDays() {
            List<Task> completed = tasks.values().stream().filter(task -> task.getCompletedAt() != null).toList();
            if (completed.isEmpty()) {
                return 0.0;
            }
            double total = completed.stream().mapToLong(Task::completionDurationDays).sum();
            return total / completed.size();
        }
    }

    private static final class InMemoryProjectRepository implements ProjectRepositoryPort {
        private final Map<UUID, Project> projects = new HashMap<>();

        @Override
        public Project save(Project project) {
            projects.put(project.getId(), project);
            return project;
        }

        @Override
        public Optional<Project> findById(UUID projectId) {
            return Optional.ofNullable(projects.get(projectId));
        }

        @Override
        public List<Project> findAll() {
            return new ArrayList<>(projects.values());
        }
    }

    private static final class InMemoryUserRepository implements UserRepositoryPort {
        private final Map<UUID, User> users = new HashMap<>();

        @Override
        public User save(User user) {
            users.put(user.getId(), user);
            return user;
        }

        @Override
        public Optional<User> findById(UUID userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users.values());
        }
    }

    private static final class InMemoryTaskHistoryRepository implements TaskHistoryRepositoryPort {
        private final List<TaskStatusHistory> entries = new ArrayList<>();

        @Override
        public TaskStatusHistory save(TaskStatusHistory history) {
            entries.add(history);
            return history;
        }

        @Override
        public List<TaskStatusHistory> findByTaskId(UUID taskId) {
            return entries.stream().filter(entry -> entry.getTaskId().equals(taskId)).toList();
        }
    }
}

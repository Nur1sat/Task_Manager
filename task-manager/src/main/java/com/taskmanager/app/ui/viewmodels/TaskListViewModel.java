package com.taskmanager.app.ui.viewmodels;

import com.taskmanager.application.dto.AnalyticsReportDto;
import com.taskmanager.application.dto.AssignTaskCommand;
import com.taskmanager.application.dto.CompleteTaskCommand;
import com.taskmanager.application.dto.CreateTaskCommand;
import com.taskmanager.application.dto.ProjectDto;
import com.taskmanager.application.dto.SearchTasksQuery;
import com.taskmanager.application.dto.TaskDto;
import com.taskmanager.application.dto.TaskHistoryDto;
import com.taskmanager.application.dto.UpdateTaskCommand;
import com.taskmanager.application.dto.UserDto;
import com.taskmanager.application.ports.in.AssignTaskInputPort;
import com.taskmanager.application.ports.in.CompleteTaskInputPort;
import com.taskmanager.application.ports.in.CreateTaskInputPort;
import com.taskmanager.application.ports.in.ReferenceDataInputPort;
import com.taskmanager.application.ports.in.SearchTasksInputPort;
import com.taskmanager.application.ports.in.TaskAnalyticsInputPort;
import com.taskmanager.application.ports.in.UpdateTaskInputPort;
import com.taskmanager.domain.model.Priority;
import com.taskmanager.domain.model.TaskStatus;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * MVVM orchestrator for task list, actions, filtering, and background loading.
 */
public class TaskListViewModel {
    private static final Logger logger = LoggerFactory.getLogger(TaskListViewModel.class);

    private final CreateTaskInputPort createTaskUseCase;
    private final UpdateTaskInputPort updateTaskUseCase;
    private final CompleteTaskInputPort completeTaskUseCase;
    private final AssignTaskInputPort assignTaskUseCase;
    private final SearchTasksInputPort searchTasksUseCase;
    private final TaskAnalyticsInputPort analyticsUseCase;
    private final ReferenceDataInputPort referenceDataUseCase;
    private final ExecutorService executor;

    private final ObservableList<ProjectOption> projects = FXCollections.observableArrayList();
    private final ObservableList<UserOption> users = FXCollections.observableArrayList();
    private final ObservableList<TaskItemViewModel> tasks = FXCollections.observableArrayList();

    private final ObjectProperty<ProjectOption> selectedProject = new SimpleObjectProperty<>();
    private final ObjectProperty<TaskStatus> selectedStatusFilter = new SimpleObjectProperty<>();
    private final ObjectProperty<Priority> selectedPriorityFilter = new SimpleObjectProperty<>();
    private final ObjectProperty<UserOption> selectedAssigneeFilter = new SimpleObjectProperty<>();
    private final ObjectProperty<TaskItemViewModel> selectedTask = new SimpleObjectProperty<>();

    private final StringProperty errorMessage = new SimpleStringProperty("");

    private volatile Map<UUID, String> userDisplayMap = Collections.emptyMap();

    public TaskListViewModel(CreateTaskInputPort createTaskUseCase,
                             UpdateTaskInputPort updateTaskUseCase,
                             CompleteTaskInputPort completeTaskUseCase,
                             AssignTaskInputPort assignTaskUseCase,
                             SearchTasksInputPort searchTasksUseCase,
                             TaskAnalyticsInputPort analyticsUseCase,
                             ReferenceDataInputPort referenceDataUseCase,
                             ExecutorService executor) {
        this.createTaskUseCase = createTaskUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.completeTaskUseCase = completeTaskUseCase;
        this.assignTaskUseCase = assignTaskUseCase;
        this.searchTasksUseCase = searchTasksUseCase;
        this.analyticsUseCase = analyticsUseCase;
        this.referenceDataUseCase = referenceDataUseCase;
        this.executor = executor;
    }

    public ObservableList<ProjectOption> projects() {
        return projects;
    }

    public ObservableList<UserOption> users() {
        return users;
    }

    public ObservableList<TaskItemViewModel> tasks() {
        return tasks;
    }

    public ObjectProperty<ProjectOption> selectedProjectProperty() {
        return selectedProject;
    }

    public ObjectProperty<TaskStatus> selectedStatusFilterProperty() {
        return selectedStatusFilter;
    }

    public ObjectProperty<Priority> selectedPriorityFilterProperty() {
        return selectedPriorityFilter;
    }

    public ObjectProperty<UserOption> selectedAssigneeFilterProperty() {
        return selectedAssigneeFilter;
    }

    public ObjectProperty<TaskItemViewModel> selectedTaskProperty() {
        return selectedTask;
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    /**
     * Loads users and projects in background, then preselects first project.
     */
    public void initialize() {
        executor.submit(() -> {
            try {
                List<ProjectDto> projectDtos = referenceDataUseCase.getProjects();
                List<UserDto> userDtos = referenceDataUseCase.getUsers();

                Map<UUID, String> localUserMap = new HashMap<>();
                for (UserDto user : userDtos) {
                    localUserMap.put(user.id(), user.username());
                }
                userDisplayMap = localUserMap;

                Platform.runLater(() -> {
                    projects.setAll(projectDtos.stream().map(p -> new ProjectOption(p.id(), p.name())).toList());
                    users.setAll(userDtos.stream().map(u -> new UserOption(u.id(), u.username())).toList());
                    if (!projects.isEmpty() && selectedProject.get() == null) {
                        selectedProject.set(projects.get(0));
                    }
                });
            } catch (Exception ex) {
                logger.error("Failed to initialize reference data", ex);
                publishError("Failed to load projects/users: " + ex.getMessage());
            }
        });
    }

    /**
     * Reloads task list based on active filters.
     */
    public void refreshTasks() {
        executor.submit(() -> {
            try {
                SearchTasksQuery query = new SearchTasksQuery(
                        selectedProject.get() == null ? null : selectedProject.get().id(),
                        selectedStatusFilter.get(),
                        selectedPriorityFilter.get(),
                        mapAssigneeFilter(selectedAssigneeFilter.get()),
                        false
                );

                List<TaskItemViewModel> list = searchTasksUseCase.execute(query).stream()
                        .map(dto -> new TaskItemViewModel(dto, resolveAssignee(dto.assigneeId())))
                        .toList();

                Platform.runLater(() -> tasks.setAll(list));
            } catch (Exception ex) {
                logger.error("Failed to refresh tasks", ex);
                publishError("Failed to refresh tasks: " + ex.getMessage());
            }
        });
    }

    /**
     * Creates a task then refreshes list and analytics.
     */
    public void createTask(NewTaskForm form, Runnable postRefreshAction) {
        executor.submit(() -> {
            try {
                createTaskUseCase.execute(new CreateTaskCommand(
                        form.projectId(),
                        form.title(),
                        form.description(),
                        form.priority(),
                        form.dueDate(),
                        form.assigneeId(),
                        form.tags(),
                        form.blockingTaskIds()
                ));
                Platform.runLater(postRefreshAction);
            } catch (Exception ex) {
                logger.error("Failed to create task", ex);
                publishError("Failed to create task: " + ex.getMessage());
            }
        });
    }

    /**
     * Completes selected task if one exists.
     */
    public void completeSelectedTask(UUID actorUserId, Runnable postRefreshAction) {
        TaskItemViewModel current = selectedTask.get();
        if (current == null) {
            publishError("Select a task first");
            return;
        }

        executor.submit(() -> {
            try {
                completeTaskUseCase.execute(new CompleteTaskCommand(current.id(), actorUserId));
                Platform.runLater(postRefreshAction);
            } catch (Exception ex) {
                logger.error("Failed to complete task", ex);
                publishError("Failed to complete task: " + ex.getMessage());
            }
        });
    }

    /**
     * Archives selected task via update use case.
     */
    public void archiveSelectedTask(Runnable postRefreshAction) {
        TaskItemViewModel current = selectedTask.get();
        if (current == null) {
            publishError("Select a task first");
            return;
        }

        executor.submit(() -> {
            try {
                TaskDto source = current.source();
                updateTaskUseCase.execute(new UpdateTaskCommand(
                        source.id(),
                        source.title(),
                        source.description(),
                        source.priority(),
                        source.status(),
                        source.dueDate(),
                        source.tags(),
                        source.blockingTaskIds(),
                        true
                ));
                Platform.runLater(postRefreshAction);
            } catch (Exception ex) {
                logger.error("Failed to archive task", ex);
                publishError("Failed to archive task: " + ex.getMessage());
            }
        });
    }

    /**
     * Assigns selected task to provided user.
     */
    public void assignSelectedTask(UserOption assignee, Runnable postRefreshAction) {
        TaskItemViewModel current = selectedTask.get();
        if (current == null) {
            publishError("Select a task first");
            return;
        }
        if (assignee == null || assignee.id() == null) {
            publishError("Select an assignee");
            return;
        }

        executor.submit(() -> {
            try {
                assignTaskUseCase.execute(new AssignTaskCommand(current.id(), assignee.id()));
                Platform.runLater(postRefreshAction);
            } catch (Exception ex) {
                logger.error("Failed to assign task", ex);
                publishError("Failed to assign task: " + ex.getMessage());
            }
        });
    }

    /**
     * Returns status history for the current selected task.
     */
    public void loadSelectedTaskDetails(TaskDetailsViewModel detailsViewModel) {
        TaskItemViewModel current = selectedTask.get();
        if (current == null) {
            Platform.runLater(detailsViewModel::clear);
            return;
        }

        executor.submit(() -> {
            try {
                List<TaskHistoryDto> history = referenceDataUseCase.getTaskHistory(current.id());
                Platform.runLater(() -> detailsViewModel.showTask(current, history));
            } catch (Exception ex) {
                logger.error("Failed to load task history", ex);
                publishError("Failed to load task details: " + ex.getMessage());
            }
        });
    }

    /**
     * Recomputes dashboard analytics.
     */
    public void refreshAnalytics(DashboardViewModel dashboardViewModel) {
        executor.submit(() -> {
            try {
                LocalDate to = LocalDate.now();
                LocalDate from = to.minusDays(6);
                AnalyticsReportDto report = analyticsUseCase.execute(from, to);
                Platform.runLater(() -> dashboardViewModel.apply(report));
            } catch (Exception ex) {
                logger.error("Failed to refresh analytics", ex);
                publishError("Failed to load analytics: " + ex.getMessage());
            }
        });
    }

    /**
     * Parses a CSV string of tags into canonical values.
     */
    public Set<String> parseTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return Collections.emptySet();
        }
        String[] parts = rawTags.split(",");
        Set<String> tags = new LinkedHashSet<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                tags.add(part.trim().toLowerCase());
            }
        }
        return tags;
    }

    private UUID mapAssigneeFilter(UserOption option) {
        if (option == null || option.id() == null) {
            return null;
        }
        return option.id();
    }

    private String resolveAssignee(UUID assigneeId) {
        if (assigneeId == null) {
            return "Unassigned";
        }
        return userDisplayMap.getOrDefault(assigneeId, assigneeId.toString());
    }

    private void publishError(String message) {
        Platform.runLater(() -> errorMessage.set(message));
    }
}

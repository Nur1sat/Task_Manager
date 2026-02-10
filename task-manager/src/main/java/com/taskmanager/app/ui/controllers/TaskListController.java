package com.taskmanager.app.ui.controllers;

import com.taskmanager.app.ui.viewmodels.DashboardViewModel;
import com.taskmanager.app.ui.viewmodels.NewTaskForm;
import com.taskmanager.app.ui.viewmodels.TaskDetailsViewModel;
import com.taskmanager.app.ui.viewmodels.TaskItemViewModel;
import com.taskmanager.app.ui.viewmodels.TaskListViewModel;
import com.taskmanager.app.ui.viewmodels.UserOption;

import java.util.Objects;
import java.util.UUID;

/**
 * Controller for task list actions and navigation events.
 */
public class TaskListController {
    private final TaskListViewModel taskListViewModel;
    private final TaskDetailsViewModel taskDetailsViewModel;
    private final DashboardViewModel dashboardViewModel;

    public TaskListController(TaskListViewModel taskListViewModel,
                              TaskDetailsViewModel taskDetailsViewModel,
                              DashboardViewModel dashboardViewModel) {
        this.taskListViewModel = Objects.requireNonNull(taskListViewModel, "taskListViewModel is required");
        this.taskDetailsViewModel = Objects.requireNonNull(taskDetailsViewModel, "taskDetailsViewModel is required");
        this.dashboardViewModel = Objects.requireNonNull(dashboardViewModel, "dashboardViewModel is required");
    }

    /**
     * Initializes the screen data.
     */
    public void initialize() {
        taskListViewModel.initialize();
    }

    /**
     * Handles task selection updates.
     */
    public void onTaskSelected(TaskItemViewModel task) {
        taskListViewModel.selectedTaskProperty().set(task);
        taskListViewModel.loadSelectedTaskDetails(taskDetailsViewModel);
    }

    /**
     * Handles filter updates and requests data refresh.
     */
    public void onFiltersChanged() {
        refreshAll();
    }

    /**
     * Creates a task from form data.
     */
    public void onCreateTask(NewTaskForm form) {
        taskListViewModel.createTask(form, this::refreshAll);
    }

    /**
     * Completes the selected task.
     */
    public void onCompleteSelected(UUID actorUserId) {
        taskListViewModel.completeSelectedTask(actorUserId, this::refreshAll);
    }

    /**
     * Archives the selected task.
     */
    public void onArchiveSelected() {
        taskListViewModel.archiveSelectedTask(this::refreshAll);
    }

    /**
     * Assigns selected task to a user.
     */
    public void onAssignSelected(UserOption userOption) {
        taskListViewModel.assignSelectedTask(userOption, this::refreshAll);
    }

    /**
     * Triggers full data refresh.
     */
    public void refreshAll() {
        taskListViewModel.refreshTasks();
        taskListViewModel.refreshAnalytics(dashboardViewModel);
        taskListViewModel.loadSelectedTaskDetails(taskDetailsViewModel);
    }
}

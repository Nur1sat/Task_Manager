package com.taskmanager.app.ui.controllers;

import com.taskmanager.app.ui.viewmodels.DashboardViewModel;
import com.taskmanager.app.ui.viewmodels.TaskListViewModel;

import java.util.Objects;

/**
 * Controller for dashboard metrics interactions.
 */
public class DashboardController {
    private final TaskListViewModel taskListViewModel;
    private final DashboardViewModel dashboardViewModel;

    public DashboardController(TaskListViewModel taskListViewModel, DashboardViewModel dashboardViewModel) {
        this.taskListViewModel = Objects.requireNonNull(taskListViewModel, "taskListViewModel is required");
        this.dashboardViewModel = Objects.requireNonNull(dashboardViewModel, "dashboardViewModel is required");
    }

    /**
     * Requests dashboard metrics refresh.
     */
    public void refresh() {
        taskListViewModel.refreshAnalytics(dashboardViewModel);
    }
}

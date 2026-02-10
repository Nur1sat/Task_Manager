package com.taskmanager.app.ui.views;

import com.taskmanager.app.ui.controllers.DashboardController;
import com.taskmanager.app.ui.controllers.TaskListController;
import com.taskmanager.app.ui.viewmodels.DashboardViewModel;
import com.taskmanager.app.ui.viewmodels.NewTaskForm;
import com.taskmanager.app.ui.viewmodels.ProjectOption;
import com.taskmanager.app.ui.viewmodels.TaskDetailsViewModel;
import com.taskmanager.app.ui.viewmodels.TaskItemViewModel;
import com.taskmanager.app.ui.viewmodels.TaskListViewModel;
import com.taskmanager.app.ui.viewmodels.UserOption;
import com.taskmanager.domain.model.TaskStatus;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JavaFX composition root for dashboard, list, filters, and details panels.
 */
public class MainView {
    private static final UserOption ALL_ASSIGNEES = new UserOption(null, "All assignees");

    private final TaskListViewModel taskListViewModel;
    private final TaskDetailsViewModel taskDetailsViewModel;
    private final DashboardViewModel dashboardViewModel;
    private final TaskListController taskListController;
    private final DashboardController dashboardController;

    public MainView(TaskListViewModel taskListViewModel,
                    TaskDetailsViewModel taskDetailsViewModel,
                    DashboardViewModel dashboardViewModel,
                    TaskListController taskListController,
                    DashboardController dashboardController) {
        this.taskListViewModel = taskListViewModel;
        this.taskDetailsViewModel = taskDetailsViewModel;
        this.dashboardViewModel = dashboardViewModel;
        this.taskListController = taskListController;
        this.dashboardController = dashboardController;
    }

    /**
     * Builds and returns the primary scene.
     */
    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));

        Label title = new Label("Task Management System");
        title.getStyleClass().add("header-title");

        ComboBox<ProjectOption> projectSelector = new ComboBox<>(taskListViewModel.projects());
        projectSelector.setPromptText("Select project");
        projectSelector.setMinWidth(240);
        projectSelector.valueProperty().bindBidirectional(taskListViewModel.selectedProjectProperty());
        projectSelector.valueProperty().addListener((obs, old, value) -> taskListController.onFiltersChanged());

        HBox topHeader = new HBox(12, title, spacer(), new Label("Project:"), projectSelector);
        topHeader.setAlignment(Pos.CENTER_LEFT);

        HBox dashboardCards = buildDashboardCards();
        VBox topContainer = new VBox(12, topHeader, dashboardCards);

        SplitPane centerContent = new SplitPane(buildTaskListPane(), buildDetailsAndCreatePane());
        centerContent.setDividerPositions(0.58);

        root.setTop(topContainer);
        root.setCenter(centerContent);

        Scene scene = new Scene(root, 1280, 820);
        URL stylesheet = getClass().getClassLoader().getResource("styles.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }

        attachErrorHandling();

        taskListController.initialize();
        taskListController.refreshAll();
        dashboardController.refresh();

        return scene;
    }

    private HBox buildDashboardCards() {
        Label contextValue = new Label();
        taskListViewModel.selectedTaskProperty().addListener((obs, old, value) -> {
            if (value == null) {
                contextValue.setText("No task selected");
            } else {
                contextValue.setText("Selected: " + value.title());
            }
        });
        contextValue.setText("No task selected");

        Label completedValue = new Label();
        completedValue.textProperty().bind(dashboardViewModel.completedLabelProperty());

        Label overdueValue = new Label();
        overdueValue.textProperty().bind(dashboardViewModel.overdueLabelProperty());

        Label averageValue = new Label();
        averageValue.textProperty().bind(dashboardViewModel.averageCompletionLabelProperty());

        VBox contextCard = metricCard("Context", contextValue);
        VBox completedCard = metricCard("Completed (7d)", completedValue);
        VBox overdueCard = metricCard("Overdue", overdueValue);
        VBox averageCard = metricCard("Avg Completion", averageValue);

        HBox cards = new HBox(12, contextCard, completedCard, overdueCard, averageCard);
        cards.setAlignment(Pos.CENTER_LEFT);
        return cards;
    }

    private VBox metricCard(String title, Label value) {
        Label heading = new Label(title);
        heading.getStyleClass().add("subtle-label");
        value.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1f2a44;");

        VBox box = new VBox(8, heading, value);
        box.getStyleClass().add("card");
        box.setMinWidth(220);
        return box;
    }

    private VBox buildTaskListPane() {
        ComboBox<TaskStatus> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(TaskStatus.values());
        statusFilter.setPromptText("All statuses");
        statusFilter.valueProperty().bindBidirectional(taskListViewModel.selectedStatusFilterProperty());
        statusFilter.valueProperty().addListener((obs, old, value) -> taskListController.onFiltersChanged());

        ComboBox<com.taskmanager.domain.model.Priority> priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll(com.taskmanager.domain.model.Priority.values());
        priorityFilter.setPromptText("All priorities");
        priorityFilter.valueProperty().bindBidirectional(taskListViewModel.selectedPriorityFilterProperty());
        priorityFilter.valueProperty().addListener((obs, old, value) -> taskListController.onFiltersChanged());

        ComboBox<UserOption> assigneeFilter = new ComboBox<>(FXCollections.observableArrayList(ALL_ASSIGNEES));
        assigneeFilter.setPromptText("All assignees");
        assigneeFilter.setValue(ALL_ASSIGNEES);
        taskListViewModel.users().addListener((ListChangeListener<UserOption>) c -> rebuildAssigneeFilter(assigneeFilter));
        rebuildAssigneeFilter(assigneeFilter);
        assigneeFilter.valueProperty().addListener((obs, old, value) -> {
            taskListViewModel.selectedAssigneeFilterProperty().set(value == ALL_ASSIGNEES ? null : value);
            taskListController.onFiltersChanged();
        });

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("secondary-button");
        refreshButton.setOnAction(event -> taskListController.refreshAll());

        HBox filters = new HBox(8,
                new Label("Status"),
                statusFilter,
                new Label("Priority"),
                priorityFilter,
                new Label("Assignee"),
                assigneeFilter,
                refreshButton
        );
        filters.setAlignment(Pos.CENTER_LEFT);

        ListView<TaskItemViewModel> taskList = new ListView<>(taskListViewModel.tasks());
        taskList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(TaskItemViewModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label title = new Label(item.title());
                title.setStyle("-fx-font-weight: 700; -fx-text-fill: #1f2a44;");

                Label meta = new Label("%s | %s | due %s | %s"
                        .formatted(item.status(), item.priority(), item.dueDate(), item.assigneeName()));
                meta.getStyleClass().add("subtle-label");

                VBox content = new VBox(4, title, meta);
                setGraphic(content);
            }
        });

        taskList.getSelectionModel().selectedItemProperty().addListener((obs, old, value) -> taskListController.onTaskSelected(value));

        ComboBox<UserOption> assignCombo = new ComboBox<>(taskListViewModel.users());
        assignCombo.setPromptText("Assign to");

        Button assignButton = new Button("Assign");
        assignButton.getStyleClass().add("secondary-button");
        assignButton.setOnAction(event -> taskListController.onAssignSelected(assignCombo.getValue()));

        Button completeButton = new Button("Complete");
        completeButton.getStyleClass().add("primary-button");
        completeButton.setOnAction(event -> taskListController.onCompleteSelected(null));

        Button archiveButton = new Button("Archive");
        archiveButton.getStyleClass().add("warning-button");
        archiveButton.setOnAction(event -> taskListController.onArchiveSelected());

        HBox actions = new HBox(8, assignCombo, assignButton, completeButton, archiveButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox panel = new VBox(10, filters, taskList, actions);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(16));
        VBox.setVgrow(taskList, Priority.ALWAYS);
        return panel;
    }

    private VBox buildDetailsAndCreatePane() {
        VBox detailsCard = buildDetailsCard();
        VBox createCard = buildCreateCard();

        VBox container = new VBox(12, detailsCard, createCard);
        VBox.setVgrow(detailsCard, Priority.ALWAYS);
        return container;
    }

    private VBox buildDetailsCard() {
        Label heading = new Label("Task Details");
        heading.getStyleClass().add("header-title");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        addDetailRow(grid, 0, "Title", taskDetailsViewModel.titleProperty());
        addDetailRow(grid, 1, "Description", taskDetailsViewModel.descriptionProperty());
        addDetailRow(grid, 2, "Status", taskDetailsViewModel.statusProperty());
        addDetailRow(grid, 3, "Priority", taskDetailsViewModel.priorityProperty());
        addDetailRow(grid, 4, "Due Date", taskDetailsViewModel.dueDateProperty());
        addDetailRow(grid, 5, "Assignee", taskDetailsViewModel.assigneeProperty());
        addDetailRow(grid, 6, "Tags", taskDetailsViewModel.tagsProperty());

        Label historyLabel = new Label("Status History");
        historyLabel.getStyleClass().add("subtle-label");

        ListView<String> historyList = new ListView<>(taskDetailsViewModel.historyEntries());
        historyList.setMinHeight(160);

        VBox card = new VBox(10, heading, grid, historyLabel, historyList);
        card.getStyleClass().add("card");
        VBox.setVgrow(historyList, Priority.ALWAYS);
        return card;
    }

    private VBox buildCreateCard() {
        Label heading = new Label("Create Task");
        heading.getStyleClass().add("header-title");

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(4);

        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(1));

        ComboBox<com.taskmanager.domain.model.Priority> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(com.taskmanager.domain.model.Priority.values());
        priorityCombo.setValue(com.taskmanager.domain.model.Priority.MEDIUM);

        ComboBox<UserOption> assigneeCombo = new ComboBox<>();
        assigneeCombo.getItems().add(UserOption.unassigned());
        assigneeCombo.getItems().addAll(taskListViewModel.users());
        assigneeCombo.setValue(UserOption.unassigned());
        taskListViewModel.users().addListener((ListChangeListener<UserOption>) change -> {
            UserOption current = assigneeCombo.getValue();
            assigneeCombo.getItems().setAll(UserOption.unassigned());
            assigneeCombo.getItems().addAll(taskListViewModel.users());
            if (current != null && assigneeCombo.getItems().contains(current)) {
                assigneeCombo.setValue(current);
            } else {
                assigneeCombo.setValue(UserOption.unassigned());
            }
        });

        TextField tagsField = new TextField();
        tagsField.setPromptText("Tags (comma separated)");

        TextField dependenciesField = new TextField();
        dependenciesField.setPromptText("Blocking Task IDs (comma separated UUIDs)");

        Button createButton = new Button("Create Task");
        createButton.getStyleClass().add("primary-button");
        createButton.setOnAction(event -> {
            ProjectOption selectedProject = taskListViewModel.selectedProjectProperty().get();
            if (selectedProject == null) {
                showError("Select a project before creating tasks");
                return;
            }

            NewTaskForm form = new NewTaskForm(
                    selectedProject.id(),
                    titleField.getText(),
                    descriptionArea.getText(),
                    priorityCombo.getValue(),
                    dueDatePicker.getValue(),
                    assigneeCombo.getValue() == null || assigneeCombo.getValue().id() == null
                            ? null
                            : assigneeCombo.getValue().id(),
                    taskListViewModel.parseTags(tagsField.getText()),
                    parseUuidCsv(dependenciesField.getText())
            );

            taskListController.onCreateTask(form);
            titleField.clear();
            descriptionArea.clear();
            tagsField.clear();
            dependenciesField.clear();
            dueDatePicker.setValue(LocalDate.now().plusDays(1));
            assigneeCombo.setValue(UserOption.unassigned());
        });

        FlowPane fields = new FlowPane(10, 10,
                labelledField("Title", titleField),
                labelledField("Description", descriptionArea),
                labelledField("Due Date", dueDatePicker),
                labelledField("Priority", priorityCombo),
                labelledField("Assignee", assigneeCombo),
                labelledField("Tags", tagsField),
                labelledField("Dependencies", dependenciesField)
        );
        fields.setPrefWrapLength(560);

        VBox card = new VBox(10, heading, fields, createButton);
        card.getStyleClass().add("card");
        return card;
    }

    private VBox labelledField(String label, javafx.scene.Node node) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("subtle-label");
        VBox box = new VBox(4, fieldLabel, node);
        box.setMinWidth(260);
        return box;
    }

    private void addDetailRow(GridPane grid, int row, String label, StringProperty valueProperty) {
        Label key = new Label(label + ":");
        key.getStyleClass().add("subtle-label");
        Label value = new Label();
        value.textProperty().bind(valueProperty);
        value.setWrapText(true);

        grid.add(key, 0, row);
        grid.add(value, 1, row);
    }

    private javafx.scene.Node spacer() {
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private Set<UUID> parseUuidCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptySet();
        }

        Set<UUID> ids = new LinkedHashSet<>();
        String[] parts = raw.split(",");
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                try {
                    ids.add(UUID.fromString(part.trim()));
                } catch (IllegalArgumentException ex) {
                    showError("Invalid dependency UUID: " + part.trim());
                    return Collections.emptySet();
                }
            }
        }
        return ids;
    }

    private void rebuildAssigneeFilter(ComboBox<UserOption> filter) {
        UserOption current = filter.getValue();
        filter.getItems().setAll(ALL_ASSIGNEES);
        filter.getItems().addAll(taskListViewModel.users());

        if (current != null && filter.getItems().contains(current)) {
            filter.setValue(current);
        } else {
            filter.setValue(ALL_ASSIGNEES);
        }
    }

    private void attachErrorHandling() {
        taskListViewModel.errorMessageProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isBlank()) {
                showError(newValue);
                taskListViewModel.errorMessageProperty().set("");
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Task Manager");
        alert.setHeaderText("Operation failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

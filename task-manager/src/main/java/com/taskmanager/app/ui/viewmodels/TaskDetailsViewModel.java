package com.taskmanager.app.ui.viewmodels;

import com.taskmanager.application.dto.TaskHistoryDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View model for the selected task details panel.
 */
public class TaskDetailsViewModel {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final StringProperty title = new SimpleStringProperty("-");
    private final StringProperty description = new SimpleStringProperty("-");
    private final StringProperty status = new SimpleStringProperty("-");
    private final StringProperty priority = new SimpleStringProperty("-");
    private final StringProperty dueDate = new SimpleStringProperty("-");
    private final StringProperty assignee = new SimpleStringProperty("-");
    private final StringProperty tags = new SimpleStringProperty("-");

    private final ObservableList<String> historyEntries = FXCollections.observableArrayList();

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty priorityProperty() {
        return priority;
    }

    public StringProperty dueDateProperty() {
        return dueDate;
    }

    public StringProperty assigneeProperty() {
        return assignee;
    }

    public StringProperty tagsProperty() {
        return tags;
    }

    public ObservableList<String> historyEntries() {
        return historyEntries;
    }

    /**
     * Updates details view fields from selected task item.
     */
    public void showTask(TaskItemViewModel item, List<TaskHistoryDto> history) {
        if (item == null) {
            clear();
            return;
        }

        title.set(item.title());
        description.set(item.description().isBlank() ? "-" : item.description());
        status.set(item.status());
        priority.set(item.priority());
        dueDate.set(item.dueDate());
        assignee.set(item.assigneeName());
        tags.set(item.tags());

        historyEntries.setAll(history.stream()
                .map(entry -> "%s -> %s at %s".formatted(
                        entry.previousStatus(),
                        entry.newStatus(),
                        entry.changedAt().format(DATE_TIME_FORMAT)))
                .toList());

        if (historyEntries.isEmpty()) {
            historyEntries.add("No status changes recorded yet");
        }
    }

    /**
     * Clears the details panel.
     */
    public void clear() {
        title.set("-");
        description.set("-");
        status.set("-");
        priority.set("-");
        dueDate.set("-");
        assignee.set("-");
        tags.set("-");
        historyEntries.setAll("No task selected");
    }
}

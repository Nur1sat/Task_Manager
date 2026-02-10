package com.taskmanager.app.ui.viewmodels;

import com.taskmanager.application.dto.AnalyticsReportDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * View model for dashboard metrics cards.
 */
public class DashboardViewModel {
    private final StringProperty completedLabel = new SimpleStringProperty("0");
    private final StringProperty overdueLabel = new SimpleStringProperty("0");
    private final StringProperty averageCompletionLabel = new SimpleStringProperty("0.0 days");

    public StringProperty completedLabelProperty() {
        return completedLabel;
    }

    public StringProperty overdueLabelProperty() {
        return overdueLabel;
    }

    public StringProperty averageCompletionLabelProperty() {
        return averageCompletionLabel;
    }

    /**
     * Applies analytics report values to UI properties.
     */
    public void apply(AnalyticsReportDto report) {
        long completed = report.completedPerDay().values().stream().mapToLong(Long::longValue).sum();
        completedLabel.set(Long.toString(completed));
        overdueLabel.set(Long.toString(report.overdueTasks()));
        averageCompletionLabel.set(String.format("%.2f days", report.averageCompletionTimeDays()));
    }
}

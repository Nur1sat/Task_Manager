package com.taskmanager.application.dto;

import java.time.LocalDate;
import java.util.Map;

/**
 * Aggregated analytics report for dashboard rendering.
 */
public record AnalyticsReportDto(
        Map<LocalDate, Long> completedPerDay,
        long overdueTasks,
        double averageCompletionTimeDays
) {
}

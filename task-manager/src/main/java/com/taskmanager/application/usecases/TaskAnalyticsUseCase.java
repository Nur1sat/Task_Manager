package com.taskmanager.application.usecases;

import com.taskmanager.application.dto.AnalyticsReportDto;
import com.taskmanager.application.ports.in.TaskAnalyticsInputPort;
import com.taskmanager.application.ports.out.TaskRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * Application service for analytics projections.
 */
public class TaskAnalyticsUseCase implements TaskAnalyticsInputPort {
    private static final Logger logger = LoggerFactory.getLogger(TaskAnalyticsUseCase.class);

    private final TaskRepositoryPort taskRepository;

    public TaskAnalyticsUseCase(TaskRepositoryPort taskRepository) {
        this.taskRepository = Objects.requireNonNull(taskRepository, "taskRepository is required");
    }

    @Override
    public AnalyticsReportDto execute(LocalDate from, LocalDate to) {
        LocalDate safeFrom = from == null ? LocalDate.now().minusDays(7) : from;
        LocalDate safeTo = to == null ? LocalDate.now() : to;

        Map<LocalDate, Long> completedPerDay = taskRepository.countCompletedPerDay(safeFrom, safeTo);
        long overdue = taskRepository.countOverdue(LocalDate.now());
        double avgCompletionDays = taskRepository.averageCompletionTimeDays();

        logger.debug("Analytics generated for {} to {}", safeFrom, safeTo);
        return new AnalyticsReportDto(completedPerDay, overdue, avgCompletionDays);
    }
}

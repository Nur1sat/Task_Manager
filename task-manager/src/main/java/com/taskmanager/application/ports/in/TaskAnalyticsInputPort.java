package com.taskmanager.application.ports.in;

import com.taskmanager.application.dto.AnalyticsReportDto;

import java.time.LocalDate;

/**
 * Input port for analytics queries.
 */
public interface TaskAnalyticsInputPort {
    AnalyticsReportDto execute(LocalDate from, LocalDate to);
}

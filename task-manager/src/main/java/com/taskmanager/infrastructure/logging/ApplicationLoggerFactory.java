package com.taskmanager.infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central logger factory for adapters and application services.
 */
public class ApplicationLoggerFactory {
    public Logger getLogger(Class<?> type) {
        return LoggerFactory.getLogger(type);
    }
}

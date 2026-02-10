package com.taskmanager.infrastructure.config;

import java.time.Clock;

/**
 * Factory for system time sources.
 */
public class ClockFactory {
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}

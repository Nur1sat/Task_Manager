package com.taskmanager.app.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Creates asynchronous executors used by view models and controllers.
 */
public class ExecutorFactory {
    public ExecutorService fixedPool(int size) {
        return Executors.newFixedThreadPool(Math.max(2, size));
    }
}

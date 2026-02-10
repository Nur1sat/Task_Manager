package com.taskmanager.app.ui.viewmodels;

import java.util.UUID;

/**
 * UI option for project selection controls.
 */
public record ProjectOption(UUID id, String name) {
    @Override
    public String toString() {
        return name;
    }
}

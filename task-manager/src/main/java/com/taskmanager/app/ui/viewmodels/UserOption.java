package com.taskmanager.app.ui.viewmodels;

import java.util.UUID;

/**
 * UI option for user selection controls.
 */
public record UserOption(UUID id, String displayName) {
    public static UserOption unassigned() {
        return new UserOption(null, "Unassigned");
    }

    @Override
    public String toString() {
        return displayName;
    }
}

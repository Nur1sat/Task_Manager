package com.taskmanager.application.dto;

import com.taskmanager.domain.model.User;

import java.util.UUID;

/**
 * User read model for UI adapters.
 */
public record UserDto(
        UUID id,
        String username,
        String email,
        boolean active
) {
    public static UserDto fromDomain(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.isActive());
    }
}

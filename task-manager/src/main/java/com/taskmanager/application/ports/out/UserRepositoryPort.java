package com.taskmanager.application.ports.out;

import com.taskmanager.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for user persistence operations.
 */
public interface UserRepositoryPort {
    User save(User user);

    Optional<User> findById(UUID userId);

    List<User> findAll();
}

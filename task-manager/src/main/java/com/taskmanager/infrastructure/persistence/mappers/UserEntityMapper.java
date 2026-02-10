package com.taskmanager.infrastructure.persistence.mappers;

import com.taskmanager.domain.model.User;
import com.taskmanager.infrastructure.persistence.entities.UserEntity;

/**
 * Maps between {@link User} and {@link UserEntity}.
 */
public class UserEntityMapper {
    public UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setActive(user.isActive());
        return entity;
    }

    public User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getEmail(), entity.isActive());
    }
}

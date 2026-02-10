package com.taskmanager.infrastructure.persistence.repositories;

import com.taskmanager.application.ports.out.UserRepositoryPort;
import com.taskmanager.domain.model.User;
import com.taskmanager.infrastructure.persistence.entities.UserEntity;
import com.taskmanager.infrastructure.persistence.mappers.UserEntityMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of {@link UserRepositoryPort}.
 */
public class JpaUserRepository implements UserRepositoryPort {
    private final JpaTransactionExecutor tx;
    private final UserEntityMapper mapper;

    public JpaUserRepository(JpaTransactionExecutor tx, UserEntityMapper mapper) {
        this.tx = tx;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        return tx.write(em -> mapper.toDomain(em.merge(mapper.toEntity(user))));
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return tx.read(em -> Optional.ofNullable(em.find(UserEntity.class, userId)).map(mapper::toDomain));
    }

    @Override
    public List<User> findAll() {
        return tx.read(em -> em.createQuery("select u from UserEntity u order by u.username", UserEntity.class)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .toList());
    }
}

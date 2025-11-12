package com.transport.user.infrastructure.persistence;

import com.transport.user.domain.model.User;
import com.transport.user.domain.model.UserRole;
import com.transport.user.domain.model.UserStatus;
import com.transport.user.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final JpaUserRepository jpaRepository;

    public UserRepositoryImpl(JpaUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    // Mappers
    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setRole(UserRoleEntity.valueOf(user.getRole().name()));
        entity.setStatus(UserStatusEntity.valueOf(user.getStatus().name()));
        entity.setCreatedAt(user.getCreatedAt());
        entity.setLastLoginAt(user.getLastLoginAt());
        return entity;
    }

    private User toDomain(UserEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setEmail(entity.getEmail());
        user.setPasswordHash(entity.getPasswordHash());
        user.setFirstName(entity.getFirstName());
        user.setLastName(entity.getLastName());
        user.setRole(UserRole.valueOf(entity.getRole().name()));
        user.setStatus(UserStatus.valueOf(entity.getStatus().name()));
        // createdAt is set in constructor, but we override it
        return user;
    }

}

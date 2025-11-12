package com.transport.user.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true)
    private String passwordHash;

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private UserRoleEntity role;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private UserStatusEntity status;

    @Column(nullable = true)
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    //Getters and setters
    public UUID getId() {return id; }
    public void setId(UUID id) {this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public UserRoleEntity getRole() { return role; }
    public void setRole(UserRoleEntity role) { this.role = role; }
    
    public UserStatusEntity getStatus() { return status; }
    public void setStatus(UserStatusEntity status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

}

enum UserRoleEntity {
    PASSENGER, DRIVER, ADMIN
}

enum UserStatusEntity {
    PENDING, ACTIVE, SUSPENDED
}

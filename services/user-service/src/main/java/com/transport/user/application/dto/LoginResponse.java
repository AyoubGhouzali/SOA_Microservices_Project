package com.transport.user.application.dto;

import com.transport.user.domain.model.UserRole;
import com.transport.user.domain.model.UserStatus;

import java.util.UUID;

public class LoginResponse {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private UserStatus status;
    private String token; // JWT token
    private String tokenType = "Bearer";
    private String message;

    public LoginResponse(UUID userId, String email, String firstName, String lastName,
                        UserRole role, UserStatus status, String token) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
        this.token = token;
        this.message = "Login successful";
    }

    // Getters and setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

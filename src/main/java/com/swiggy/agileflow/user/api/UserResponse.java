package com.swiggy.agileflow.user.api;

import com.swiggy.agileflow.user.domain.User;
import java.time.Instant;

public record UserResponse(
    Long id,
    String username,
    String email,
    String displayName,
    boolean active,
    Instant createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(),
            u.getDisplayName(), u.isActive(), u.getCreatedAt());
    }
}

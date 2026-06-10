package com.swiggy.agileflow.user.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank String username,
    @NotBlank @Email String email,
    @NotBlank String displayName,
    String password
) {}

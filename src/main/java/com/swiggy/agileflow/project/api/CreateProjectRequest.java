package com.swiggy.agileflow.project.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateProjectRequest(
    @NotBlank @Pattern(regexp = "[A-Z0-9]{2,10}", message = "Project key must be 2-10 uppercase letters/digits")
    String projectKey,
    @NotBlank String name,
    String description,
    Long leadUserId
) {}

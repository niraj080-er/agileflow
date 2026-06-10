package com.swiggy.agileflow.project.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddProjectMemberRequest(
    @NotNull Long userId,
    @Pattern(regexp = "LEAD|MEMBER", message = "Role must be LEAD or MEMBER")
    String role
) {}

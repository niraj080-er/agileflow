package com.swiggy.agileflow.workflow.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWorkflowTransitionRequest(
    @NotNull Long fromStatusId,
    @NotNull Long toStatusId,
    @NotBlank String name
) {}

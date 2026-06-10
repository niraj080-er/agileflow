package com.swiggy.agileflow.workflow.api;

import com.swiggy.agileflow.workflow.domain.StatusCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWorkflowStatusRequest(
    @NotBlank String name,
    @NotNull StatusCategory category,
    Integer orderIndex
) {}

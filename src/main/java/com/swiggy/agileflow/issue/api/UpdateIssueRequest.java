package com.swiggy.agileflow.issue.api;

import com.swiggy.agileflow.issue.domain.Priority;
import jakarta.validation.constraints.NotNull;

/**
 * Partial update of an issue. Only non-null fields are applied. {@code version}
 * is required and must match the issue's current version for optimistic-lock
 * conflict detection (mismatch -> 409).
 */
public record UpdateIssueRequest(
    @NotNull Long version,
    String title,
    String description,
    Long assigneeId,
    Priority priority,
    Integer storyPoints,
    Long statusId
) {
}

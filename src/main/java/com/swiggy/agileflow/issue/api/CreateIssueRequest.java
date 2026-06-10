package com.swiggy.agileflow.issue.api;

import com.swiggy.agileflow.issue.domain.IssueType;
import com.swiggy.agileflow.issue.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request to create an issue. {@code statusId} is optional — when omitted the
 * project's first workflow status (lowest order) is used.
 */
public record CreateIssueRequest(
    @NotNull Long projectId,
    @NotNull IssueType type,
    @NotBlank String title,
    String description,
    @NotNull Long reporterId,
    Long assigneeId,
    Priority priority,
    Integer storyPoints,
    Long parentIssueId,
    Long statusId
) {
}

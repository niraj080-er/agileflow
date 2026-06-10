package com.swiggy.agileflow.issue.api;

import com.swiggy.agileflow.issue.domain.IssueType;
import com.swiggy.agileflow.issue.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/projects/{projectId}/issues}.
 * The project is taken from the path; all other fields mirror {@link CreateIssueRequest}.
 */
public record CreateIssueInProjectRequest(
    @NotNull IssueType type,
    @NotBlank String title,
    String description,
    @NotNull Long reporterId,
    Long assigneeId,
    Priority priority,
    Integer storyPoints,
    Long parentIssueId,
    Long statusId
) {}

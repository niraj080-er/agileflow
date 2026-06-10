package com.swiggy.agileflow.issue.api;

import com.swiggy.agileflow.issue.domain.Issue;
import com.swiggy.agileflow.issue.domain.IssueType;
import com.swiggy.agileflow.issue.domain.Priority;
import java.time.Instant;

/** Issue representation returned by the API. */
public record IssueResponse(
    Long id,
    Long projectId,
    String issueKey,
    IssueType type,
    String title,
    String description,
    Long statusId,
    Long assigneeId,
    Long reporterId,
    Priority priority,
    Integer storyPoints,
    Long parentIssueId,
    Long version,
    Instant createdAt,
    Instant updatedAt
) {
    public static IssueResponse from(Issue i) {
        return new IssueResponse(
            i.getId(), i.getProjectId(), i.getIssueKey(), i.getType(), i.getTitle(),
            i.getDescription(), i.getStatusId(), i.getAssigneeId(), i.getReporterId(),
            i.getPriority(), i.getStoryPoints(), i.getParentIssueId(), i.getVersion(),
            i.getCreatedAt(), i.getUpdatedAt());
    }
}

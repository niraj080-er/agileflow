package com.swiggy.agileflow.search.application;

import com.swiggy.agileflow.issue.domain.IssueType;
import com.swiggy.agileflow.issue.domain.Priority;

public record IssueSearchCriteria(
    Long projectId,
    String query,
    Long statusId,
    Long assigneeId,
    IssueType type,
    Priority priority,
    String cursor,
    int limit
) {}

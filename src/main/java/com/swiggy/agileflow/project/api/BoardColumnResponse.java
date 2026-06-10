package com.swiggy.agileflow.project.api;

import com.swiggy.agileflow.issue.api.IssueResponse;
import com.swiggy.agileflow.workflow.domain.StatusCategory;
import java.util.List;

public record BoardColumnResponse(
    Long statusId,
    String statusName,
    StatusCategory category,
    int orderIndex,
    List<IssueResponse> issues
) {}

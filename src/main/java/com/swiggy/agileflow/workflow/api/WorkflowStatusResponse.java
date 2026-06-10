package com.swiggy.agileflow.workflow.api;

import com.swiggy.agileflow.workflow.domain.StatusCategory;
import com.swiggy.agileflow.workflow.domain.WorkflowStatus;
import java.time.Instant;

public record WorkflowStatusResponse(
    Long id,
    Long projectId,
    String name,
    StatusCategory category,
    int orderIndex,
    Instant createdAt
) {
    public static WorkflowStatusResponse from(WorkflowStatus s) {
        return new WorkflowStatusResponse(s.getId(), s.getProjectId(), s.getName(),
            s.getCategory(), s.getOrderIndex(), s.getCreatedAt());
    }
}

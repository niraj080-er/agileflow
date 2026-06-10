package com.swiggy.agileflow.workflow.api;

import com.swiggy.agileflow.workflow.domain.WorkflowTransition;
import java.time.Instant;

public record WorkflowTransitionResponse(
    Long id,
    Long projectId,
    Long fromStatusId,
    Long toStatusId,
    String name,
    Instant createdAt
) {
    public static WorkflowTransitionResponse from(WorkflowTransition t) {
        return new WorkflowTransitionResponse(t.getId(), t.getProjectId(), t.getFromStatusId(),
            t.getToStatusId(), t.getName(), t.getCreatedAt());
    }
}

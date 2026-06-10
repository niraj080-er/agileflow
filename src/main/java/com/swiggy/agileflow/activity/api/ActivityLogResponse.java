package com.swiggy.agileflow.activity.api;

import com.swiggy.agileflow.activity.domain.ActivityActionType;
import com.swiggy.agileflow.activity.domain.ActivityLog;
import java.time.Instant;

public record ActivityLogResponse(
    Long id,
    Long projectId,
    Long issueId,
    Long actorId,
    ActivityActionType actionType,
    String fieldName,
    String oldValue,
    String newValue,
    Instant createdAt
) {
    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(
            log.getId(),
            log.getProjectId(),
            log.getIssueId(),
            log.getActorId(),
            log.getActionType(),
            log.getFieldName(),
            log.getOldValue(),
            log.getNewValue(),
            log.getCreatedAt()
        );
    }
}

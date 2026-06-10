package com.swiggy.agileflow.activity.application;

import com.swiggy.agileflow.activity.domain.ActivityActionType;
import com.swiggy.agileflow.activity.domain.ActivityLog;
import com.swiggy.agileflow.activity.infrastructure.ActivityLogRepository;
import org.springframework.stereotype.Service;

/** Persists audit entries to the append-only {@code activity_logs} table. */
@Service
public class ActivityLogRecorder implements ActivityLogPort {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogRecorder(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    public void record(Long projectId, Long issueId, Long actorId, ActivityActionType actionType,
                       String fieldName, String oldValue, String newValue) {
        ActivityLog log = new ActivityLog();
        log.setProjectId(projectId);
        log.setIssueId(issueId);
        log.setActorId(actorId);
        log.setActionType(actionType);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        activityLogRepository.save(log);
    }
}

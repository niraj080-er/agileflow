package com.swiggy.agileflow.activity.application;

import com.swiggy.agileflow.activity.domain.ActivityActionType;

/**
 * Port for recording audit entries. Application services depend on this
 * abstraction so the audit mechanism can evolve independently.
 */
public interface ActivityLogPort {

    /**
     * Records a single audit entry.
     *
     * @param projectId  owning project
     * @param issueId    target issue (nullable for project-level events)
     * @param actorId    user performing the action
     * @param actionType type of action
     * @param fieldName  changed field, or null
     * @param oldValue   previous value, or null
     * @param newValue   new value, or null
     */
    void record(Long projectId, Long issueId, Long actorId, ActivityActionType actionType,
                String fieldName, String oldValue, String newValue);
}

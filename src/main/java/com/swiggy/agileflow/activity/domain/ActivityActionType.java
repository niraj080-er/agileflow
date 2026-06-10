package com.swiggy.agileflow.activity.domain;

/** Types of auditable actions recorded in the activity log. */
public enum ActivityActionType {
    ISSUE_CREATED,
    FIELD_CHANGED,
    STATUS_CHANGED,
    ASSIGNED,
    COMMENT_ADDED,
    SPRINT_STARTED,
    SPRINT_COMPLETED
}

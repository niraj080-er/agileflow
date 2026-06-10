package com.swiggy.agileflow.realtime.domain;

/** Real-time event types broadcast to connected clients. */
public enum RealtimeEventType {
    ISSUE_CREATED,
    ISSUE_UPDATED,
    ISSUE_MOVED,
    COMMENT_ADDED,
    SPRINT_UPDATED
}

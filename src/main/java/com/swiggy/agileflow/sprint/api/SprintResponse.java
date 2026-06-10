package com.swiggy.agileflow.sprint.api;

import com.swiggy.agileflow.sprint.domain.Sprint;
import com.swiggy.agileflow.sprint.domain.SprintState;
import java.time.Instant;
import java.time.LocalDate;

public record SprintResponse(
    Long id,
    Long projectId,
    String name,
    String goal,
    SprintState state,
    LocalDate startDate,
    LocalDate endDate,
    Instant completedAt,
    Instant createdAt
) {
    public static SprintResponse from(Sprint sprint) {
        return new SprintResponse(
            sprint.getId(),
            sprint.getProjectId(),
            sprint.getName(),
            sprint.getGoal(),
            sprint.getState(),
            sprint.getStartDate(),
            sprint.getEndDate(),
            sprint.getCompletedAt(),
            sprint.getCreatedAt()
        );
    }
}

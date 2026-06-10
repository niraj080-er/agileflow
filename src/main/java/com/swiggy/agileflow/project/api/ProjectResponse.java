package com.swiggy.agileflow.project.api;

import com.swiggy.agileflow.project.domain.Project;
import java.time.Instant;

public record ProjectResponse(
    Long id,
    String projectKey,
    String name,
    String description,
    Long leadUserId,
    Instant createdAt
) {
    public static ProjectResponse from(Project p) {
        return new ProjectResponse(p.getId(), p.getProjectKey(), p.getName(),
            p.getDescription(), p.getLeadUserId(), p.getCreatedAt());
    }
}

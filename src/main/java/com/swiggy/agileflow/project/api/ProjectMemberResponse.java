package com.swiggy.agileflow.project.api;

import com.swiggy.agileflow.project.domain.ProjectMember;
import java.time.Instant;

public record ProjectMemberResponse(
    Long id,
    Long projectId,
    Long userId,
    String role,
    Instant createdAt
) {
    public static ProjectMemberResponse from(ProjectMember m) {
        return new ProjectMemberResponse(m.getId(), m.getProjectId(),
            m.getUserId(), m.getRole(), m.getCreatedAt());
    }
}

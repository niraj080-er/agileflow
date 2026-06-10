package com.swiggy.agileflow.issue.api;

import jakarta.validation.constraints.NotNull;

/** Request body for POST /api/issues/{id}/transitions. */
public record TransitionIssueRequest(
    @NotNull Long targetStatusId,
    @NotNull Long version
) {
}

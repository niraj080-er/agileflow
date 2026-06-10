package com.swiggy.agileflow.project.api;

import java.util.List;

public record BoardResponse(
    Long projectId,
    Long sprintId,
    List<BoardColumnResponse> columns
) {}

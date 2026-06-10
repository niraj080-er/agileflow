package com.swiggy.agileflow.comment.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddCommentRequest(
    @NotNull Long authorId,
    @NotBlank String body
) {}

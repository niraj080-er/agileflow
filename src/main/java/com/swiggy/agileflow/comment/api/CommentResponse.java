package com.swiggy.agileflow.comment.api;

import com.swiggy.agileflow.comment.domain.Comment;
import java.time.Instant;

public record CommentResponse(
    Long id,
    Long issueId,
    Long authorId,
    String body,
    Instant createdAt,
    Instant updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getIssueId(),
            comment.getAuthorId(),
            comment.getBody(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}

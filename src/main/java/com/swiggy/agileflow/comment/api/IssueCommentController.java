package com.swiggy.agileflow.comment.api;

import com.swiggy.agileflow.comment.application.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
@Tag(name = "Issue Comments", description = "Add and list comments on issues.")
public class IssueCommentController {

    private final CommentService commentService;

    public IssueCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a comment to an issue",
        description = "Creates a comment and notifies watchers. Returns 404 for missing issue or author.")
    public CommentResponse addComment(@PathVariable Long id,
                                      @Valid @RequestBody AddCommentRequest request) {
        return commentService.addComment(id, request);
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "List comments on an issue",
        description = "Returns all comments ordered by id ASC. Returns 404 for missing issue.")
    public List<CommentResponse> listComments(@PathVariable Long id) {
        return commentService.listComments(id);
    }
}

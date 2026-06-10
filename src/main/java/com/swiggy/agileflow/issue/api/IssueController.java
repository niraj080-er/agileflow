package com.swiggy.agileflow.issue.api;

import com.swiggy.agileflow.common.pagination.CursorPage;
import com.swiggy.agileflow.issue.application.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP surface for issues. Delegates all behavior to {@link IssueService};
 * holds no business logic and never touches persistence directly.
 */
@RestController
@RequestMapping("/api/issues")
@Tag(name = "Issues", description = "Create, read, list, and update work items.")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an issue",
        description = "Validates issue-type hierarchy rules. Returns 422 for an illegal parent/child pairing, "
            + "404 for a missing project/user/parent.")
    public IssueResponse create(@Valid @RequestBody CreateIssueRequest request) {
        return issueService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an issue by id", description = "Returns 404 if the issue does not exist.")
    public IssueResponse get(@PathVariable Long id) {
        return issueService.get(id);
    }

    @GetMapping
    @Operation(summary = "List issues in a project (cursor-paginated)")
    public CursorPage<IssueResponse> list(
            @RequestParam Long projectId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return issueService.list(projectId, cursor, limit);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an issue",
        description = "Optimistic-locked via the required version field (mismatch -> 409). "
            + "Status changes are validated against the project workflow (illegal transition -> 422).")
    public IssueResponse update(@PathVariable Long id, @Valid @RequestBody UpdateIssueRequest request) {
        return issueService.update(id, request);
    }

    @PostMapping("/{id}/transitions")
    @Operation(summary = "Transition issue status",
        description = "Moves the issue to a new workflow status. "
            + "Validates the transition against project workflow rules (invalid transition -> 422). "
            + "Optimistic-locked via the required version field (mismatch -> 409). "
            + "Returns 404 for missing issue or status.")
    public IssueResponse transition(@PathVariable Long id,
                                    @Valid @RequestBody TransitionIssueRequest request) {
        return issueService.transition(id, request);
    }
}

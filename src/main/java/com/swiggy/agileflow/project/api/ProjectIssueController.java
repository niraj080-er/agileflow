package com.swiggy.agileflow.project.api;

import com.swiggy.agileflow.issue.api.CreateIssueInProjectRequest;
import com.swiggy.agileflow.issue.api.IssueResponse;
import com.swiggy.agileflow.issue.application.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project-scoped issue endpoints. Delegates all behavior to {@link IssueService};
 * holds no business logic and never touches persistence directly.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/issues")
@Tag(name = "Project Issues", description = "Create issues within a specific project.")
public class ProjectIssueController {

    private final IssueService issueService;

    public ProjectIssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create an issue in a project",
        description = "Creates a work item within the project identified by {projectId}. "
            + "Returns 422 for illegal parent/child type pairings or business-rule violations, "
            + "404 for missing project, user, parent issue, or workflow status.")
    public IssueResponse create(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateIssueInProjectRequest request) {
        return issueService.create(projectId, request);
    }
}

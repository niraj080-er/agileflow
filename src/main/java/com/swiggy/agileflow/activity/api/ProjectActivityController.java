package com.swiggy.agileflow.activity.api;

import com.swiggy.agileflow.activity.application.ActivityFeedService;
import com.swiggy.agileflow.common.pagination.CursorPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Project Activity", description = "Audit trail of project and issue mutations.")
public class ProjectActivityController {

    private final ActivityFeedService activityFeedService;

    public ProjectActivityController(ActivityFeedService activityFeedService) {
        this.activityFeedService = activityFeedService;
    }

    @GetMapping("/{id}/activity")
    @Operation(summary = "List project activity",
        description = "Returns cursor-paginated activity log entries for a project, ascending by id. "
            + "Optional issueId filter restricts to a single issue. "
            + "Returns 404 for missing project or issue, 422 if issue belongs to another project.")
    public CursorPage<ActivityLogResponse> listActivity(
            @PathVariable Long id,
            @RequestParam(required = false) Long issueId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return activityFeedService.listActivity(id, issueId, cursor, limit);
    }
}

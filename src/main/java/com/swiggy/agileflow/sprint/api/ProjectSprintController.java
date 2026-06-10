package com.swiggy.agileflow.sprint.api;

import com.swiggy.agileflow.sprint.application.SprintService;
import com.swiggy.agileflow.sprint.domain.SprintState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Sprints", description = "Manage project sprints.")
public class ProjectSprintController {

    private final SprintService sprintService;

    public ProjectSprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping("/{id}/sprints")
    @Operation(summary = "List project sprints",
        description = "Returns all sprints for a project, ordered by creation date ascending. "
            + "Optional state filter (FUTURE, ACTIVE, COMPLETED). Returns 404 for missing project.")
    public List<SprintResponse> listSprints(
            @PathVariable Long id,
            @RequestParam(required = false) SprintState state) {
        return sprintService.listSprints(id, state);
    }
}

package com.swiggy.agileflow.sprint.api;

import com.swiggy.agileflow.sprint.application.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sprints")
@Tag(name = "Sprints", description = "Manage project sprints.")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a sprint",
        description = "Transitions a FUTURE sprint to ACTIVE. "
            + "Only one sprint per project may be active at a time (422 if another is active). "
            + "End date must be after start date (422). Returns 404 for missing sprint.")
    public SprintResponse startSprint(@PathVariable Long id,
                                      @Valid @RequestBody StartSprintRequest request) {
        return sprintService.startSprint(id, request);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete a sprint",
        description = "Transitions an ACTIVE sprint to COMPLETED. "
            + "Incomplete issues are moved to the target sprint if provided, or dropped from the sprint. "
            + "Returns 422 if sprint is not ACTIVE or target sprint rules are violated. Returns 404 for missing sprint.")
    public SprintResponse completeSprint(@PathVariable Long id,
                                         @RequestBody(required = false) CompleteSprintRequest request) {
        return sprintService.completeSprint(id, request);
    }
}

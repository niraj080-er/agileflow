package com.swiggy.agileflow.workflow.api;

import com.swiggy.agileflow.workflow.application.WorkflowService;
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
@RequestMapping("/api/projects/{projectId}/workflow")
@Tag(name = "Workflow", description = "Configure workflow statuses and transitions per project.")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/statuses")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a workflow status")
    public WorkflowStatusResponse createStatus(@PathVariable Long projectId,
                                               @Valid @RequestBody CreateWorkflowStatusRequest request) {
        return workflowService.createStatus(projectId, request);
    }

    @GetMapping("/statuses")
    @Operation(summary = "List workflow statuses for a project")
    public List<WorkflowStatusResponse> listStatuses(@PathVariable Long projectId) {
        return workflowService.listStatuses(projectId);
    }

    @PostMapping("/transitions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a workflow transition")
    public WorkflowTransitionResponse createTransition(@PathVariable Long projectId,
                                                       @Valid @RequestBody CreateWorkflowTransitionRequest request) {
        return workflowService.createTransition(projectId, request);
    }

    @GetMapping("/transitions")
    @Operation(summary = "List workflow transitions for a project")
    public List<WorkflowTransitionResponse> listTransitions(@PathVariable Long projectId) {
        return workflowService.listTransitions(projectId);
    }
}

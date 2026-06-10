package com.swiggy.agileflow.project.api;

import com.swiggy.agileflow.project.application.ProjectService;
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
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Create and manage projects and their members.")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a project")
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request) {
        return projectService.create(request);
    }

    @GetMapping
    @Operation(summary = "List all projects")
    public List<ProjectResponse> list() {
        return projectService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by id")
    public ProjectResponse get(@PathVariable Long id) {
        return projectService.get(id);
    }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a member to a project")
    public ProjectMemberResponse addMember(@PathVariable Long id,
                                           @Valid @RequestBody AddProjectMemberRequest request) {
        return projectService.addMember(id, request);
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "List members of a project")
    public List<ProjectMemberResponse> listMembers(@PathVariable Long id) {
        return projectService.listMembers(id);
    }
}

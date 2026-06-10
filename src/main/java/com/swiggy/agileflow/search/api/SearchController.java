package com.swiggy.agileflow.search.api;

import com.swiggy.agileflow.common.pagination.CursorPage;
import com.swiggy.agileflow.issue.api.IssueResponse;
import com.swiggy.agileflow.issue.domain.IssueType;
import com.swiggy.agileflow.issue.domain.Priority;
import com.swiggy.agileflow.search.application.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Full-text search across project issues with optional structured filters.")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    @Operation(summary = "Search issues",
        description = "Full-text search over issue title and description within a project. "
            + "Supports optional filters: statusId, assigneeId, type, priority. "
            + "Returns 400 if query is missing or fewer than 2 characters. "
            + "Returns 404 for missing project or assignee. "
            + "Returns 422 if statusId does not belong to the project.")
    public CursorPage<IssueResponse> search(
            @RequestParam Long projectId,
            @RequestParam String q,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) IssueType type,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return searchService.search(projectId, q, statusId, assigneeId, type, priority, cursor, limit);
    }
}

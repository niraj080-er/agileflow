package com.swiggy.agileflow.search.application;

import com.swiggy.agileflow.common.error.BusinessRuleException;
import com.swiggy.agileflow.common.error.NotFoundException;
import com.swiggy.agileflow.common.pagination.CursorPage;
import com.swiggy.agileflow.issue.api.IssueResponse;
import com.swiggy.agileflow.issue.domain.IssueType;
import com.swiggy.agileflow.issue.domain.Priority;
import com.swiggy.agileflow.project.infrastructure.ProjectRepository;
import com.swiggy.agileflow.user.infrastructure.UserRepository;
import com.swiggy.agileflow.workflow.infrastructure.WorkflowStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SearchPort searchPort;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkflowStatusRepository statusRepository;

    public SearchService(SearchPort searchPort,
                         ProjectRepository projectRepository,
                         UserRepository userRepository,
                         WorkflowStatusRepository statusRepository) {
        this.searchPort = searchPort;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.statusRepository = statusRepository;
    }

    @Transactional(readOnly = true)
    public CursorPage<IssueResponse> search(Long projectId, String query, Long statusId,
                                            Long assigneeId, IssueType type, Priority priority,
                                            String cursor, int limit) {
        if (!projectRepository.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }
        if (query == null || query.trim().length() < 2) {
            throw new BusinessRuleException("Search query must be at least 2 characters.");
        }
        if (assigneeId != null && !userRepository.existsById(assigneeId)) {
            throw NotFoundException.of("Assignee", assigneeId);
        }
        if (statusId != null) {
            var status = statusRepository.findById(statusId)
                .orElseThrow(() -> NotFoundException.of("Workflow status", statusId));
            if (!status.getProjectId().equals(projectId)) {
                throw new BusinessRuleException("Status does not belong to this project.");
            }
        }

        int pageSize = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        var criteria = new IssueSearchCriteria(projectId, query.trim(), statusId, assigneeId,
            type, priority, cursor, pageSize);
        var page = searchPort.searchIssues(criteria);
        return CursorPage.of(
            page.items().stream().map(IssueResponse::from).toList(),
            page.nextCursor());
    }
}

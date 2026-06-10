package com.swiggy.agileflow.project.application;

import com.swiggy.agileflow.common.error.BusinessRuleException;
import com.swiggy.agileflow.common.error.NotFoundException;
import com.swiggy.agileflow.issue.api.IssueResponse;
import com.swiggy.agileflow.issue.domain.Issue;
import com.swiggy.agileflow.issue.infrastructure.IssueRepository;
import com.swiggy.agileflow.project.api.BoardColumnResponse;
import com.swiggy.agileflow.project.api.BoardResponse;
import com.swiggy.agileflow.project.infrastructure.ProjectRepository;
import com.swiggy.agileflow.sprint.domain.SprintIssue;
import com.swiggy.agileflow.sprint.infrastructure.SprintIssueRepository;
import com.swiggy.agileflow.sprint.infrastructure.SprintRepository;
import com.swiggy.agileflow.workflow.domain.WorkflowStatus;
import com.swiggy.agileflow.workflow.infrastructure.WorkflowStatusRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final SprintRepository sprintRepository;
    private final SprintIssueRepository sprintIssueRepository;
    private final WorkflowStatusRepository statusRepository;

    public BoardService(ProjectRepository projectRepository,
                        IssueRepository issueRepository,
                        SprintRepository sprintRepository,
                        SprintIssueRepository sprintIssueRepository,
                        WorkflowStatusRepository statusRepository) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.sprintRepository = sprintRepository;
        this.sprintIssueRepository = sprintIssueRepository;
        this.statusRepository = statusRepository;
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoard(Long projectId, Long sprintId) {
        if (!projectRepository.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }

        if (sprintId != null) {
            var sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> NotFoundException.of("Sprint", sprintId));
            if (!sprint.getProjectId().equals(projectId)) {
                throw new BusinessRuleException("Sprint does not belong to this project.");
            }
        }

        List<WorkflowStatus> statuses = statusRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
        List<Issue> issues = loadIssues(projectId, sprintId);

        Map<Long, List<Issue>> byStatus = issues.stream()
            .collect(Collectors.groupingBy(Issue::getStatusId));

        List<BoardColumnResponse> columns = statuses.stream()
            .map(s -> new BoardColumnResponse(
                s.getId(),
                s.getName(),
                s.getCategory(),
                s.getOrderIndex(),
                byStatus.getOrDefault(s.getId(), List.of()).stream()
                    .map(IssueResponse::from)
                    .toList()
            ))
            .toList();

        return new BoardResponse(projectId, sprintId, columns);
    }

    private List<Issue> loadIssues(Long projectId, Long sprintId) {
        if (sprintId == null) {
            return issueRepository.findByProjectId(projectId);
        }
        List<SprintIssue> sprintIssues = sprintIssueRepository.findBySprintId(sprintId);
        if (sprintIssues.isEmpty()) {
            return List.of();
        }
        Set<Long> issueIds = sprintIssues.stream().map(SprintIssue::getIssueId).collect(Collectors.toSet());
        return issueRepository.findAllById(issueIds);
    }
}

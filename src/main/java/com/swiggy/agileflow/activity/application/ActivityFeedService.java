package com.swiggy.agileflow.activity.application;

import com.swiggy.agileflow.activity.api.ActivityLogResponse;
import com.swiggy.agileflow.activity.domain.ActivityLog;
import com.swiggy.agileflow.activity.infrastructure.ActivityLogRepository;
import com.swiggy.agileflow.common.error.BusinessRuleException;
import com.swiggy.agileflow.common.error.NotFoundException;
import com.swiggy.agileflow.common.pagination.CursorPage;
import com.swiggy.agileflow.common.pagination.Cursors;
import com.swiggy.agileflow.issue.infrastructure.IssueRepository;
import com.swiggy.agileflow.project.infrastructure.ProjectRepository;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityFeedService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;

    public ActivityFeedService(ActivityLogRepository activityLogRepository,
                               ProjectRepository projectRepository,
                               IssueRepository issueRepository) {
        this.activityLogRepository = activityLogRepository;
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
    }

    @Transactional(readOnly = true)
    public CursorPage<ActivityLogResponse> listActivity(Long projectId, Long issueId,
                                                        String cursor, int limit) {
        if (!projectRepository.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }
        if (issueId != null) {
            var issue = issueRepository.findById(issueId)
                .orElseThrow(() -> NotFoundException.of("Issue", issueId));
            if (!issue.getProjectId().equals(projectId)) {
                throw new BusinessRuleException("Issue does not belong to this project.");
            }
        }

        int pageSize = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        long after = Cursors.decode(cursor);

        List<ActivityLog> logs = issueId == null
            ? activityLogRepository.findByProjectIdAndIdGreaterThanOrderByIdAsc(
                projectId, after, Limit.of(pageSize))
            : activityLogRepository.findByProjectIdAndIssueIdAndIdGreaterThanOrderByIdAsc(
                projectId, issueId, after, Limit.of(pageSize));

        List<ActivityLogResponse> items = logs.stream().map(ActivityLogResponse::from).toList();
        String nextCursor = logs.size() == pageSize
            ? Cursors.encode(logs.get(logs.size() - 1).getId())
            : null;
        return CursorPage.of(items, nextCursor);
    }
}

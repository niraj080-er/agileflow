package com.swiggy.agileflow.issue.application;

import com.swiggy.agileflow.activity.application.ActivityLogPort;
import com.swiggy.agileflow.activity.domain.ActivityActionType;
import com.swiggy.agileflow.common.error.BusinessRuleException;
import com.swiggy.agileflow.common.error.NotFoundException;
import com.swiggy.agileflow.common.pagination.CursorPage;
import com.swiggy.agileflow.common.pagination.Cursors;
import com.swiggy.agileflow.issue.api.CreateIssueInProjectRequest;
import com.swiggy.agileflow.issue.api.CreateIssueRequest;
import com.swiggy.agileflow.issue.api.IssueResponse;
import com.swiggy.agileflow.issue.api.TransitionIssueRequest;
import com.swiggy.agileflow.issue.api.UpdateIssueRequest;
import com.swiggy.agileflow.issue.domain.Issue;
import com.swiggy.agileflow.issue.domain.IssueHierarchyPolicy;
import com.swiggy.agileflow.issue.domain.Priority;
import com.swiggy.agileflow.issue.infrastructure.IssueRepository;
import com.swiggy.agileflow.project.domain.Project;
import com.swiggy.agileflow.project.infrastructure.ProjectRepository;
import com.swiggy.agileflow.realtime.application.RealtimeBroadcaster;
import com.swiggy.agileflow.realtime.domain.RealtimeEventType;
import com.swiggy.agileflow.user.infrastructure.UserRepository;
import com.swiggy.agileflow.workflow.domain.WorkflowStatus;
import com.swiggy.agileflow.workflow.domain.WorkflowTransitionValidator;
import com.swiggy.agileflow.workflow.infrastructure.WorkflowStatusRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issue use cases. Orchestrates persistence, hierarchy and workflow rules,
 * optimistic-locking conflict detection, audit logging, and real-time
 * broadcasting. Holds no HTTP concerns and no direct SQL.
 */
@Service
public class IssueService {

    private static final int MAX_PAGE_SIZE = 100;

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkflowStatusRepository statusRepository;
    private final IssueHierarchyPolicy hierarchyPolicy;
    private final WorkflowTransitionValidator transitionValidator;
    private final ActivityLogPort activityLog;
    private final RealtimeBroadcaster broadcaster;

    public IssueService(IssueRepository issueRepository,
                        ProjectRepository projectRepository,
                        UserRepository userRepository,
                        WorkflowStatusRepository statusRepository,
                        IssueHierarchyPolicy hierarchyPolicy,
                        WorkflowTransitionValidator transitionValidator,
                        ActivityLogPort activityLog,
                        RealtimeBroadcaster broadcaster) {
        this.issueRepository = issueRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.statusRepository = statusRepository;
        this.hierarchyPolicy = hierarchyPolicy;
        this.transitionValidator = transitionValidator;
        this.activityLog = activityLog;
        this.broadcaster = broadcaster;
    }

    @Transactional
    public IssueResponse create(Long projectId, CreateIssueInProjectRequest req) {
        return create(new CreateIssueRequest(projectId, req.type(), req.title(), req.description(),
            req.reporterId(), req.assigneeId(), req.priority(),
            req.storyPoints(), req.parentIssueId(), req.statusId()));
    }

    @Transactional
    public IssueResponse create(CreateIssueRequest req) {
        Project project = projectRepository.findById(req.projectId())
            .orElseThrow(() -> NotFoundException.of("Project", req.projectId()));
        requireUser(req.reporterId(), "Reporter");
        if (req.assigneeId() != null) {
            requireUser(req.assigneeId(), "Assignee");
        }

        WorkflowStatus status = resolveInitialStatus(project.getId(), req.statusId());

        Issue parent = null;
        if (req.parentIssueId() != null) {
            parent = issueRepository.findById(req.parentIssueId())
                .orElseThrow(() -> NotFoundException.of("Parent issue", req.parentIssueId()));
            if (!parent.getProjectId().equals(project.getId())) {
                throw new BusinessRuleException("Parent issue belongs to a different project.");
            }
        }
        hierarchyPolicy.validateParent(req.type(), parent == null ? null : parent.getType());

        Issue issue = new Issue();
        issue.setProjectId(project.getId());
        issue.setIssueKey(nextIssueKey(project));
        issue.setType(req.type());
        issue.setTitle(req.title());
        issue.setDescription(req.description());
        issue.setStatusId(status.getId());
        issue.setReporterId(req.reporterId());
        issue.setAssigneeId(req.assigneeId());
        issue.setPriority(req.priority() == null ? Priority.MEDIUM : req.priority());
        issue.setStoryPoints(req.storyPoints());
        issue.setParentIssueId(parent == null ? null : parent.getId());

        Issue saved = issueRepository.save(issue);

        activityLog.record(project.getId(), saved.getId(), req.reporterId(),
            ActivityActionType.ISSUE_CREATED, null, null, saved.getIssueKey());
        broadcaster.broadcast(project.getId(), RealtimeEventType.ISSUE_CREATED, saved.getIssueKey());

        return IssueResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public IssueResponse get(Long id) {
        return IssueResponse.from(loadIssue(id));
    }

    @Transactional(readOnly = true)
    public CursorPage<IssueResponse> list(Long projectId, String cursor, int limit) {
        int pageSize = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        long after = Cursors.decode(cursor);
        List<Issue> issues = issueRepository
            .findByProjectIdAndIdGreaterThanOrderByIdAsc(projectId, after, Limit.of(pageSize));
        List<IssueResponse> items = issues.stream().map(IssueResponse::from).toList();
        String nextCursor = issues.size() == pageSize
            ? Cursors.encode(issues.get(issues.size() - 1).getId())
            : null;
        return CursorPage.of(items, nextCursor);
    }

    @Transactional
    public IssueResponse update(Long id, UpdateIssueRequest req) {
        Issue issue = loadIssue(id);
        Long actorId = issue.getReporterId(); // no auth in the foundation; reporter stands in as actor

        if (!Objects.equals(issue.getVersion(), req.version())) {
            throw new OptimisticLockingFailureException(
                "Issue " + issue.getIssueKey() + " was modified concurrently.");
        }

        boolean statusChanged = false;

        if (req.title() != null && !req.title().equals(issue.getTitle())) {
            recordChange(issue, actorId, "title", issue.getTitle(), req.title());
            issue.setTitle(req.title());
        }
        if (req.description() != null && !req.description().equals(issue.getDescription())) {
            recordChange(issue, actorId, "description", issue.getDescription(), req.description());
            issue.setDescription(req.description());
        }
        if (req.priority() != null && req.priority() != issue.getPriority()) {
            recordChange(issue, actorId, "priority",
                String.valueOf(issue.getPriority()), String.valueOf(req.priority()));
            issue.setPriority(req.priority());
        }
        if (req.storyPoints() != null && !req.storyPoints().equals(issue.getStoryPoints())) {
            recordChange(issue, actorId, "storyPoints",
                String.valueOf(issue.getStoryPoints()), String.valueOf(req.storyPoints()));
            issue.setStoryPoints(req.storyPoints());
        }
        if (req.assigneeId() != null && !req.assigneeId().equals(issue.getAssigneeId())) {
            requireUser(req.assigneeId(), "Assignee");
            activityLog.record(issue.getProjectId(), issue.getId(), actorId,
                ActivityActionType.ASSIGNED, "assigneeId",
                String.valueOf(issue.getAssigneeId()), String.valueOf(req.assigneeId()));
            issue.setAssigneeId(req.assigneeId());
        }
        if (req.statusId() != null && !req.statusId().equals(issue.getStatusId())) {
            WorkflowStatus target = loadStatusInProject(issue.getProjectId(), req.statusId());
            transitionValidator.validateTransition(issue.getProjectId(), issue.getStatusId(), target.getId());
            activityLog.record(issue.getProjectId(), issue.getId(), actorId,
                ActivityActionType.STATUS_CHANGED, "statusId",
                String.valueOf(issue.getStatusId()), String.valueOf(target.getId()));
            issue.setStatusId(target.getId());
            statusChanged = true;
        }

        Issue saved = issueRepository.save(issue);
        broadcaster.broadcast(saved.getProjectId(),
            statusChanged ? RealtimeEventType.ISSUE_MOVED : RealtimeEventType.ISSUE_UPDATED,
            saved.getIssueKey());
        return IssueResponse.from(saved);
    }

    @Transactional
    public IssueResponse transition(Long issueId, TransitionIssueRequest req) {
        Issue issue = loadIssue(issueId);
        Long actorId = issue.getReporterId();

        if (!Objects.equals(issue.getVersion(), req.version())) {
            throw new OptimisticLockingFailureException(
                "Issue " + issue.getIssueKey() + " was modified concurrently.");
        }

        WorkflowStatus target = loadStatusInProject(issue.getProjectId(), req.targetStatusId());
        transitionValidator.validateTransition(issue.getProjectId(), issue.getStatusId(), target.getId());

        if (!target.getId().equals(issue.getStatusId())) {
            activityLog.record(issue.getProjectId(), issue.getId(), actorId,
                ActivityActionType.STATUS_CHANGED, "statusId",
                String.valueOf(issue.getStatusId()), String.valueOf(target.getId()));
            issue.setStatusId(target.getId());
        }

        Issue saved = issueRepository.save(issue);
        broadcaster.broadcast(saved.getProjectId(), RealtimeEventType.ISSUE_MOVED, saved.getIssueKey());
        return IssueResponse.from(saved);
    }

    // --- helpers -------------------------------------------------------------

    private Issue loadIssue(Long id) {
        return issueRepository.findById(id)
            .orElseThrow(() -> NotFoundException.of("Issue", id));
    }

    private void requireUser(Long userId, String label) {
        if (!userRepository.existsById(userId)) {
            throw NotFoundException.of(label, userId);
        }
    }

    private WorkflowStatus resolveInitialStatus(Long projectId, Long statusId) {
        if (statusId != null) {
            return loadStatusInProject(projectId, statusId);
        }
        return statusRepository.findByProjectIdOrderByOrderIndexAsc(projectId).stream()
            .findFirst()
            .orElseThrow(() -> new BusinessRuleException(
                "Project has no workflow statuses configured."));
    }

    private WorkflowStatus loadStatusInProject(Long projectId, Long statusId) {
        WorkflowStatus status = statusRepository.findById(statusId)
            .orElseThrow(() -> NotFoundException.of("Workflow status", statusId));
        if (!status.getProjectId().equals(projectId)) {
            throw new BusinessRuleException("Workflow status does not belong to this project.");
        }
        return status;
    }

    private String nextIssueKey(Project project) {
        // Sequential per-project key. The (project_id, issue_key) unique constraint
        // guards against the rare concurrent-collision case (surfaced as 409).
        long next = issueRepository.countByProjectId(project.getId()) + 1;
        return project.getProjectKey() + "-" + next;
    }

    private void recordChange(Issue issue, Long actorId, String field, String oldVal, String newVal) {
        activityLog.record(issue.getProjectId(), issue.getId(), actorId,
            ActivityActionType.FIELD_CHANGED, field, oldVal, newVal);
    }
}

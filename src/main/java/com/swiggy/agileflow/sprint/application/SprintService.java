package com.swiggy.agileflow.sprint.application;

import com.swiggy.agileflow.common.error.BusinessRuleException;
import com.swiggy.agileflow.common.error.NotFoundException;
import com.swiggy.agileflow.issue.domain.Issue;
import com.swiggy.agileflow.issue.infrastructure.IssueRepository;
import com.swiggy.agileflow.project.infrastructure.ProjectRepository;
import com.swiggy.agileflow.realtime.application.RealtimeBroadcaster;
import com.swiggy.agileflow.realtime.domain.RealtimeEventType;
import com.swiggy.agileflow.sprint.api.CompleteSprintRequest;
import com.swiggy.agileflow.sprint.api.SprintResponse;
import com.swiggy.agileflow.sprint.api.StartSprintRequest;
import com.swiggy.agileflow.sprint.domain.Sprint;
import com.swiggy.agileflow.sprint.domain.SprintIssue;
import com.swiggy.agileflow.sprint.domain.SprintState;
import com.swiggy.agileflow.sprint.infrastructure.SprintIssueRepository;
import com.swiggy.agileflow.sprint.infrastructure.SprintRepository;
import com.swiggy.agileflow.workflow.domain.StatusCategory;
import com.swiggy.agileflow.workflow.infrastructure.WorkflowStatusRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final SprintIssueRepository sprintIssueRepository;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final WorkflowStatusRepository statusRepository;
    private final RealtimeBroadcaster broadcaster;

    public SprintService(SprintRepository sprintRepository,
                         SprintIssueRepository sprintIssueRepository,
                         ProjectRepository projectRepository,
                         IssueRepository issueRepository,
                         WorkflowStatusRepository statusRepository,
                         RealtimeBroadcaster broadcaster) {
        this.sprintRepository = sprintRepository;
        this.sprintIssueRepository = sprintIssueRepository;
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.statusRepository = statusRepository;
        this.broadcaster = broadcaster;
    }

    @Transactional(readOnly = true)
    public List<SprintResponse> listSprints(Long projectId, SprintState state) {
        if (!projectRepository.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }
        List<Sprint> sprints = state == null
            ? sprintRepository.findByProjectIdOrderByCreatedAtAsc(projectId)
            : sprintRepository.findByProjectIdAndStateOrderByCreatedAtAsc(projectId, state);
        return sprints.stream().map(SprintResponse::from).toList();
    }

    @Transactional
    public SprintResponse startSprint(Long sprintId, StartSprintRequest req) {
        Sprint sprint = loadSprint(sprintId);
        if (sprint.getState() != SprintState.FUTURE) {
            throw new BusinessRuleException(
                "Only a FUTURE sprint can be started. Current state: " + sprint.getState());
        }
        if (!req.endDate().isAfter(req.startDate())) {
            throw new BusinessRuleException("End date must be after start date.");
        }
        if (sprintRepository.existsByProjectIdAndState(sprint.getProjectId(), SprintState.ACTIVE)) {
            throw new BusinessRuleException(
                "Project already has an active sprint. Complete it before starting a new one.");
        }
        sprint.setState(SprintState.ACTIVE);
        sprint.setStartDate(req.startDate());
        sprint.setEndDate(req.endDate());
        Sprint saved = sprintRepository.save(sprint);
        broadcaster.broadcast(saved.getProjectId(), RealtimeEventType.SPRINT_UPDATED, saved.getName());
        return SprintResponse.from(saved);
    }

    @Transactional
    public SprintResponse completeSprint(Long sprintId, CompleteSprintRequest req) {
        Sprint sprint = loadSprint(sprintId);
        if (sprint.getState() != SprintState.ACTIVE) {
            throw new BusinessRuleException(
                "Only an ACTIVE sprint can be completed. Current state: " + sprint.getState());
        }

        Sprint targetSprint = null;
        if (req != null && req.targetSprintId() != null) {
            if (req.targetSprintId().equals(sprintId)) {
                throw new BusinessRuleException("Target sprint cannot be the sprint being completed.");
            }
            targetSprint = sprintRepository.findById(req.targetSprintId())
                .orElseThrow(() -> NotFoundException.of("Target sprint", req.targetSprintId()));
            if (!targetSprint.getProjectId().equals(sprint.getProjectId())) {
                throw new BusinessRuleException("Target sprint belongs to a different project.");
            }
            if (targetSprint.getState() != SprintState.FUTURE) {
                throw new BusinessRuleException("Target sprint must be in FUTURE state.");
            }
        }

        List<SprintIssue> sprintIssues = sprintIssueRepository.findBySprintId(sprintId);
        if (!sprintIssues.isEmpty()) {
            List<Long> issueIds = sprintIssues.stream().map(SprintIssue::getIssueId).toList();
            List<Issue> issues = issueRepository.findAllById(issueIds);

            Set<Long> doneStatusIds = statusRepository.findByProjectIdOrderByOrderIndexAsc(sprint.getProjectId())
                .stream()
                .filter(s -> s.getCategory() == StatusCategory.DONE)
                .map(s -> s.getId())
                .collect(Collectors.toSet());

            Map<Boolean, List<SprintIssue>> partitioned = sprintIssues.stream()
                .collect(Collectors.partitioningBy(si -> {
                    return issues.stream()
                        .filter(i -> i.getId().equals(si.getIssueId()))
                        .findFirst()
                        .map(i -> doneStatusIds.contains(i.getStatusId()))
                        .orElse(false);
                }));

            List<SprintIssue> incomplete = partitioned.get(false);
            if (!incomplete.isEmpty()) {
                List<Long> incompleteIds = incomplete.stream().map(SprintIssue::getId).toList();
                sprintIssueRepository.deleteAllById(incompleteIds);
                if (targetSprint != null) {
                    Long targetSprintId = targetSprint.getId();
                    List<SprintIssue> moved = incomplete.stream().map(si -> {
                        SprintIssue newSi = new SprintIssue();
                        newSi.setSprintId(targetSprintId);
                        newSi.setIssueId(si.getIssueId());
                        newSi.setAddedAt(Instant.now());
                        return newSi;
                    }).toList();
                    sprintIssueRepository.saveAll(moved);
                }
            }

        }

        sprint.setState(SprintState.COMPLETED);
        sprint.setCompletedAt(Instant.now());
        Sprint saved = sprintRepository.save(sprint);
        broadcaster.broadcast(saved.getProjectId(), RealtimeEventType.SPRINT_UPDATED, saved.getName());
        return SprintResponse.from(saved);
    }

    private Sprint loadSprint(Long id) {
        return sprintRepository.findById(id)
            .orElseThrow(() -> NotFoundException.of("Sprint", id));
    }
}

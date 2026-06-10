package com.swiggy.agileflow.workflow.application;

import com.swiggy.agileflow.common.error.BusinessRuleException;
import com.swiggy.agileflow.common.error.NotFoundException;
import com.swiggy.agileflow.project.infrastructure.ProjectRepository;
import com.swiggy.agileflow.workflow.api.CreateWorkflowStatusRequest;
import com.swiggy.agileflow.workflow.api.CreateWorkflowTransitionRequest;
import com.swiggy.agileflow.workflow.api.WorkflowStatusResponse;
import com.swiggy.agileflow.workflow.api.WorkflowTransitionResponse;
import com.swiggy.agileflow.workflow.domain.WorkflowStatus;
import com.swiggy.agileflow.workflow.domain.WorkflowTransition;
import com.swiggy.agileflow.workflow.infrastructure.WorkflowStatusRepository;
import com.swiggy.agileflow.workflow.infrastructure.WorkflowTransitionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowService {

    private final WorkflowStatusRepository statusRepository;
    private final WorkflowTransitionRepository transitionRepository;
    private final ProjectRepository projectRepository;

    public WorkflowService(WorkflowStatusRepository statusRepository,
                           WorkflowTransitionRepository transitionRepository,
                           ProjectRepository projectRepository) {
        this.statusRepository = statusRepository;
        this.transitionRepository = transitionRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public WorkflowStatusResponse createStatus(Long projectId, CreateWorkflowStatusRequest req) {
        if (!projectRepository.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }
        List<WorkflowStatus> existing = statusRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
        boolean nameTaken = existing.stream().anyMatch(s -> s.getName().equalsIgnoreCase(req.name()));
        if (nameTaken) {
            throw new BusinessRuleException("Status '" + req.name() + "' already exists in this project.");
        }
        int order = req.orderIndex() != null ? req.orderIndex() : existing.size() + 1;
        WorkflowStatus status = new WorkflowStatus();
        status.setProjectId(projectId);
        status.setName(req.name());
        status.setCategory(req.category());
        status.setOrderIndex(order);
        return WorkflowStatusResponse.from(statusRepository.save(status));
    }

    @Transactional(readOnly = true)
    public List<WorkflowStatusResponse> listStatuses(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }
        return statusRepository.findByProjectIdOrderByOrderIndexAsc(projectId)
            .stream().map(WorkflowStatusResponse::from).toList();
    }

    @Transactional
    public WorkflowTransitionResponse createTransition(Long projectId, CreateWorkflowTransitionRequest req) {
        if (!projectRepository.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }
        WorkflowStatus from = statusRepository.findById(req.fromStatusId())
            .orElseThrow(() -> NotFoundException.of("Workflow status", req.fromStatusId()));
        WorkflowStatus to = statusRepository.findById(req.toStatusId())
            .orElseThrow(() -> NotFoundException.of("Workflow status", req.toStatusId()));
        if (!from.getProjectId().equals(projectId) || !to.getProjectId().equals(projectId)) {
            throw new BusinessRuleException("Both statuses must belong to project " + projectId);
        }
        if (from.getId().equals(to.getId())) {
            throw new BusinessRuleException("From and To status must be different.");
        }
        if (transitionRepository.existsByProjectIdAndFromStatusIdAndToStatusId(
                projectId, from.getId(), to.getId())) {
            throw new BusinessRuleException("Transition from '" + from.getName() +
                "' to '" + to.getName() + "' already exists.");
        }
        WorkflowTransition transition = new WorkflowTransition();
        transition.setProjectId(projectId);
        transition.setFromStatusId(from.getId());
        transition.setToStatusId(to.getId());
        transition.setName(req.name());
        return WorkflowTransitionResponse.from(transitionRepository.save(transition));
    }

    @Transactional(readOnly = true)
    public List<WorkflowTransitionResponse> listTransitions(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw NotFoundException.of("Project", projectId);
        }
        return transitionRepository.findByProjectId(projectId)
            .stream().map(WorkflowTransitionResponse::from).toList();
    }
}

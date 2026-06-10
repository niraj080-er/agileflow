package com.swiggy.agileflow.workflow.application;

import com.swiggy.agileflow.common.error.BusinessRuleException;
import com.swiggy.agileflow.workflow.domain.WorkflowTransitionValidator;
import com.swiggy.agileflow.workflow.infrastructure.WorkflowTransitionRepository;
import org.springframework.stereotype.Service;

/**
 * Validates transitions against the project's configured
 * {@code workflow_transitions}. A no-op move (same status) is always allowed.
 */
@Service
public class DefaultWorkflowTransitionValidator implements WorkflowTransitionValidator {

    private final WorkflowTransitionRepository transitionRepository;

    public DefaultWorkflowTransitionValidator(WorkflowTransitionRepository transitionRepository) {
        this.transitionRepository = transitionRepository;
    }

    @Override
    public void validateTransition(Long projectId, Long fromStatusId, Long toStatusId) {
        if (fromStatusId.equals(toStatusId)) {
            return;
        }
        boolean allowed = transitionRepository
            .existsByProjectIdAndFromStatusIdAndToStatusId(projectId, fromStatusId, toStatusId);
        if (!allowed) {
            throw new BusinessRuleException(
                "Transition from status " + fromStatusId + " to " + toStatusId
                    + " is not allowed for this project.");
        }
    }
}

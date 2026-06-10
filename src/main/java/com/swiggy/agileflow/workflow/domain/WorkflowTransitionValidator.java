package com.swiggy.agileflow.workflow.domain;

/**
 * Port for validating whether an issue may move between two statuses within a
 * project. Implementations may layer additional guard rules (e.g. required
 * fields, role checks) without changing issue logic — the Open/Closed seam for
 * the workflow engine.
 */
public interface WorkflowTransitionValidator {

    /**
     * @throws com.swiggy.agileflow.common.error.BusinessRuleException if the
     *         transition is not permitted (maps to 422)
     */
    void validateTransition(Long projectId, Long fromStatusId, Long toStatusId);
}

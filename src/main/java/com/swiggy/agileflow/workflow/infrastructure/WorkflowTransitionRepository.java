package com.swiggy.agileflow.workflow.infrastructure;

import com.swiggy.agileflow.workflow.domain.WorkflowTransition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {

    boolean existsByProjectIdAndFromStatusIdAndToStatusId(Long projectId, Long fromStatusId, Long toStatusId);

    List<WorkflowTransition> findByProjectIdAndFromStatusId(Long projectId, Long fromStatusId);

    List<WorkflowTransition> findByProjectId(Long projectId);
}

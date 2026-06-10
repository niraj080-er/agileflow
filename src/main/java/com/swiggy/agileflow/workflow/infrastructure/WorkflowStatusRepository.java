package com.swiggy.agileflow.workflow.infrastructure;

import com.swiggy.agileflow.workflow.domain.WorkflowStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowStatusRepository extends JpaRepository<WorkflowStatus, Long> {

    List<WorkflowStatus> findByProjectIdOrderByOrderIndexAsc(Long projectId);

    Optional<WorkflowStatus> findByProjectIdAndName(Long projectId, String name);
}

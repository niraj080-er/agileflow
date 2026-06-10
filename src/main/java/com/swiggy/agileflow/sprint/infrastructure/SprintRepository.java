package com.swiggy.agileflow.sprint.infrastructure;

import com.swiggy.agileflow.sprint.domain.Sprint;
import com.swiggy.agileflow.sprint.domain.SprintState;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProjectId(Long projectId);

    List<Sprint> findByProjectIdOrderByCreatedAtAsc(Long projectId);

    List<Sprint> findByProjectIdAndStateOrderByCreatedAtAsc(Long projectId, SprintState state);

    boolean existsByProjectIdAndState(Long projectId, SprintState state);
}

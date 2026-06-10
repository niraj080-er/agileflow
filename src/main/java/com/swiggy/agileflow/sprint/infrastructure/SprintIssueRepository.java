package com.swiggy.agileflow.sprint.infrastructure;

import com.swiggy.agileflow.sprint.domain.SprintIssue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintIssueRepository extends JpaRepository<SprintIssue, Long> {

    List<SprintIssue> findBySprintId(Long sprintId);

    List<SprintIssue> findByIssueId(Long issueId);

    void deleteBySprintIdAndIssueIdIn(Long sprintId, List<Long> issueIds);
}

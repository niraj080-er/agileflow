package com.swiggy.agileflow.activity.infrastructure;

import com.swiggy.agileflow.activity.domain.ActivityLog;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByIssueIdOrderByIdAsc(Long issueId);

    /** Keyset page of a project's activity feed, ascending by id. */
    List<ActivityLog> findByProjectIdAndIdGreaterThanOrderByIdAsc(Long projectId, Long id, Limit limit);

    /** Keyset page filtered by issueId. */
    List<ActivityLog> findByProjectIdAndIssueIdAndIdGreaterThanOrderByIdAsc(
        Long projectId, Long issueId, Long id, Limit limit);
}

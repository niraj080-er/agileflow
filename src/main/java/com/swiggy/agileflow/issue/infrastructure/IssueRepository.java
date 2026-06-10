package com.swiggy.agileflow.issue.infrastructure;

import com.swiggy.agileflow.issue.domain.Issue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    Optional<Issue> findByProjectIdAndIssueKey(Long projectId, String issueKey);

    long countByProjectId(Long projectId);

    /** Keyset page: issues in a project with id strictly greater than the cursor, ascending. */
    List<Issue> findByProjectIdAndIdGreaterThanOrderByIdAsc(Long projectId, Long id, Limit limit);

    List<Issue> findByProjectId(Long projectId);
}

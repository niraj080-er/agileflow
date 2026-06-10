package com.swiggy.agileflow.issue.infrastructure;

import com.swiggy.agileflow.issue.domain.IssueLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueLinkRepository extends JpaRepository<IssueLink, Long> {

    List<IssueLink> findBySourceIssueId(Long sourceIssueId);
}

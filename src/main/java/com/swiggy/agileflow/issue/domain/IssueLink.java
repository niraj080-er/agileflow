package com.swiggy.agileflow.issue.domain;

import com.swiggy.agileflow.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "issue_links")
public class IssueLink extends BaseEntity {

    @Column(name = "source_issue_id", nullable = false)
    private Long sourceIssueId;

    @Column(name = "target_issue_id", nullable = false)
    private Long targetIssueId;

    @Column(name = "link_type", nullable = false)
    private String linkType;
}

package com.swiggy.agileflow.issue.domain;

import com.swiggy.agileflow.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "issues")
public class Issue extends AuditableEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "issue_key", nullable = false)
    private String issueKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueType type;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(name = "status_id", nullable = false)
    private Long statusId;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "parent_issue_id")
    private Long parentIssueId;

    @Version
    @Column(nullable = false)
    private Long version;
}

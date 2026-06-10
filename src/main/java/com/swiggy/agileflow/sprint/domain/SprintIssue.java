package com.swiggy.agileflow.sprint.domain;

import com.swiggy.agileflow.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sprint_issues")
public class SprintIssue extends BaseEntity {

    @Column(name = "sprint_id", nullable = false)
    private Long sprintId;

    @Column(name = "issue_id", nullable = false)
    private Long issueId;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}

package com.swiggy.agileflow.workflow.domain;

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
@Table(name = "workflow_transitions")
public class WorkflowTransition extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "from_status_id", nullable = false)
    private Long fromStatusId;

    @Column(name = "to_status_id", nullable = false)
    private Long toStatusId;

    @Column(nullable = false)
    private String name;
}

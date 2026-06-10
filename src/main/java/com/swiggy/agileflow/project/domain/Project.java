package com.swiggy.agileflow.project.domain;

import com.swiggy.agileflow.common.domain.AuditableEntity;
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
@Table(name = "projects")
public class Project extends AuditableEntity {

    @Column(name = "project_key", nullable = false, unique = true)
    private String projectKey;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "lead_user_id")
    private Long leadUserId;
}

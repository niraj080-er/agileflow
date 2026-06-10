package com.swiggy.agileflow.project.infrastructure;

import com.swiggy.agileflow.project.domain.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByProjectKey(String projectKey);
}

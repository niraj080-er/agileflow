package com.swiggy.agileflow.project.infrastructure;

import com.swiggy.agileflow.project.domain.CustomFieldDefinition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomFieldDefinitionRepository extends JpaRepository<CustomFieldDefinition, Long> {

    List<CustomFieldDefinition> findByProjectId(Long projectId);
}

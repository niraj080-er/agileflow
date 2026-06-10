package com.swiggy.agileflow.project.infrastructure;

import com.swiggy.agileflow.project.domain.CustomFieldValue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, Long> {

    List<CustomFieldValue> findByIssueId(Long issueId);
}

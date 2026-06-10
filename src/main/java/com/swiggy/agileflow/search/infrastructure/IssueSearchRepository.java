package com.swiggy.agileflow.search.infrastructure;

import com.swiggy.agileflow.issue.domain.Issue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Backs {@link com.swiggy.agileflow.search.application.SearchPort} with a
 * PostgreSQL full-text query using the GIN index on issue title/description.
 */
public interface IssueSearchRepository extends JpaRepository<Issue, Long> {

    @Query(value = """
        SELECT * FROM issues i
        WHERE i.project_id = :projectId
          AND i.id > :cursor
          AND to_tsvector('english', coalesce(i.title, '') || ' ' || coalesce(i.description, ''))
              @@ plainto_tsquery('english', :query)
        ORDER BY i.id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Issue> fullTextSearch(@Param("projectId") Long projectId,
                               @Param("query") String query,
                               @Param("cursor") long cursor,
                               @Param("limit") int limit);

    @Query(value = """
        SELECT * FROM issues i
        WHERE i.project_id = :projectId
          AND i.id > :cursor
          AND to_tsvector('english', coalesce(i.title, '') || ' ' || coalesce(i.description, ''))
              @@ plainto_tsquery('english', :query)
          AND (:statusId IS NULL OR i.status_id = :statusId)
          AND (:assigneeId IS NULL OR i.assignee_id = :assigneeId)
          AND (:type IS NULL OR i.type = :type)
          AND (:priority IS NULL OR i.priority = :priority)
        ORDER BY i.id ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Issue> fullTextSearchFiltered(@Param("projectId") Long projectId,
                                       @Param("query") String query,
                                       @Param("cursor") long cursor,
                                       @Param("statusId") Long statusId,
                                       @Param("assigneeId") Long assigneeId,
                                       @Param("type") String type,
                                       @Param("priority") String priority,
                                       @Param("limit") int limit);
}

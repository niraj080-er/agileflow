package com.swiggy.agileflow.search.application;

import com.swiggy.agileflow.common.pagination.CursorPage;
import com.swiggy.agileflow.issue.domain.Issue;

/**
 * Port for issue search. Hidden behind an abstraction so the implementation
 * (DB full-text now, an external engine later) can change without touching
 * callers. Foundation skeleton — structured filters and ranking arrive with
 * the search API spec.
 */
public interface SearchPort {

    /**
     * Full-text search across issue title/description for a project.
     *
     * @param projectId project to search within
     * @param query     free-text query
     * @param cursor    opaque pagination cursor (nullable for first page)
     * @param limit     max results
     */
    CursorPage<Issue> searchIssues(Long projectId, String query, String cursor, int limit);

    /** Full-text search with optional structured filters. */
    CursorPage<Issue> searchIssues(IssueSearchCriteria criteria);
}

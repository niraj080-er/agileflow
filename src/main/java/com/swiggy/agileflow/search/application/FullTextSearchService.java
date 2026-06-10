package com.swiggy.agileflow.search.application;

import com.swiggy.agileflow.common.pagination.CursorPage;
import com.swiggy.agileflow.common.pagination.Cursors;
import com.swiggy.agileflow.issue.domain.Issue;
import com.swiggy.agileflow.search.infrastructure.IssueSearchRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Foundation skeleton: PostgreSQL full-text search over issues with cursor
 * pagination. Structured filters (status, assignee, sprint, labels) and ranking
 * are layered on by the search API spec.
 */
@Service
public class FullTextSearchService implements SearchPort {

    private final IssueSearchRepository searchRepository;

    public FullTextSearchService(IssueSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPage<Issue> searchIssues(Long projectId, String query, String cursor, int limit) {
        long after = Cursors.decode(cursor);
        List<Issue> results = searchRepository.fullTextSearch(projectId, query, after, limit);
        String nextCursor = results.size() == limit
            ? Cursors.encode(results.get(results.size() - 1).getId())
            : null;
        return CursorPage.of(results, nextCursor);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPage<Issue> searchIssues(IssueSearchCriteria c) {
        long after = Cursors.decode(c.cursor());
        List<Issue> results = searchRepository.fullTextSearchFiltered(
            c.projectId(), c.query(), after,
            c.statusId(), c.assigneeId(),
            c.type() == null ? null : c.type().name(),
            c.priority() == null ? null : c.priority().name(),
            c.limit());
        String nextCursor = results.size() == c.limit()
            ? Cursors.encode(results.get(results.size() - 1).getId())
            : null;
        return CursorPage.of(results, nextCursor);
    }
}

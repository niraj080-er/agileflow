package com.swiggy.agileflow.common.pagination;

import java.util.List;

/**
 * A page of results for cursor-based pagination.
 *
 * @param items      the items on this page
 * @param nextCursor opaque cursor to fetch the next page, or {@code null} if this is the last page
 */
public record CursorPage<T>(List<T> items, String nextCursor) {

    public static <T> CursorPage<T> of(List<T> items, String nextCursor) {
        return new CursorPage<>(items, nextCursor);
    }
}

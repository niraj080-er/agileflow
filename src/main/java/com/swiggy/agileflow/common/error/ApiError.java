package com.swiggy.agileflow.common.error;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error response body returned across all APIs.
 */
public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<String> details
) {
    public static ApiError of(int status, String error, String message, String path, List<String> details) {
        return new ApiError(Instant.now(), status, error, message, path, details == null ? List.of() : details);
    }
}

package com.swiggy.agileflow.common.error;

/**
 * Thrown on a state conflict such as a duplicate unique value or a concurrent
 * modification detected outside JPA's own optimistic locking. Maps to HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

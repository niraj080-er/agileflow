package com.swiggy.agileflow.common.error;

/**
 * Thrown when a request is well-formed and the referenced resources exist, but
 * a domain/business rule forbids the operation (e.g. an invalid workflow
 * transition or an illegal issue-type parent-child pairing). Maps to HTTP 422.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}

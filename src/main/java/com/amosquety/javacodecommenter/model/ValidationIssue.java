package com.amosquety.javacodecommenter.model;

import java.util.Objects;

/**
 * Represents a single validation problem found in a method's Javadoc.
 */
public record ValidationIssue(
        String methodName,
        int lineNumber,
        Severity severity,
        String message
) {

    /**
     * Severity level of a validation issue.
     */
    public enum Severity { ERROR, WARNING }

    /**
     * Creates a validation issue.
     *
     * @param methodName the method the issue relates to
     * @param lineNumber the 1-based line of the method
     * @param severity   the issue severity
     * @param message    a human-readable description of the problem
     */
    public ValidationIssue {
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(message, "message");
    }

    /**
     * Returns a single-line representation of the issue.
     *
     * @return formatted severity, line, method, and message
     */
    @Override
    public String toString() {
        return String.format("[%s] Line %d - %s(): %s", severity, lineNumber, methodName, message);
    }
}
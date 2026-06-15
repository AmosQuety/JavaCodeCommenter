package com.amosquety.javacodecommenter.model;

import java.util.Objects;

/**
 * Represents a single validation problem found in a method's Javadoc.
 */
public class ValidationIssue {

    /**
     * Severity level of a validation issue.
     */
    public enum Severity { ERROR, WARNING }

    private String methodName;
    private int lineNumber;
    private Severity severity;
    private String message;

    /**
     * Creates a validation issue.
     *
     * @param methodName the method the issue relates to
     * @param lineNumber the 1-based line of the method
     * @param severity   the issue severity
     * @param message    a human-readable description of the problem
     */
    public ValidationIssue(String methodName, int lineNumber, Severity severity, String message) {
        this.methodName = Objects.requireNonNull(methodName, "methodName");
        this.lineNumber = lineNumber;
        this.severity = Objects.requireNonNull(severity, "severity");
        this.message = Objects.requireNonNull(message, "message");
    }

    /**
     * Returns the name of the method the issue relates to.
     *
     * @return the method name
     */
    public String getMethodName() { return methodName; }

    /**
     * Returns the 1-based line of the affected method.
     *
     * @return the line number
     */
    public int getLineNumber() { return lineNumber; }

    /**
     * Returns the severity of the issue.
     *
     * @return the severity
     */
    public Severity getSeverity() { return severity; }

    /**
     * Returns the human-readable description of the problem.
     *
     * @return the message
     */
    public String getMessage() { return message; }

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
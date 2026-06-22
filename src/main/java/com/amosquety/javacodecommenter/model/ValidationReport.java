package com.amosquety.javacodecommenter.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregates all validation issues found in a source file.
 */
public record ValidationReport(
        String filePath,
        List<ValidationIssue> issues,
        int totalMethods
) {

    /**
     * Creates a validation report for a file.
     *
     * @param filePath     path to the validated source file
     * @param issues       all issues found in the file
     * @param totalMethods total number of methods examined
     */
    public ValidationReport {
        Objects.requireNonNull(filePath, "filePath");
        Objects.requireNonNull(issues, "issues");
    }

    /**
     * Returns all issues found in the file.
     *
     * @return the validation issues
     */
    public List<ValidationIssue> getIssues() { return Collections.unmodifiableList(issues); }

    /**
     * Counts the issues with {@link ValidationIssue.Severity#ERROR} severity.
     *
     * @return the number of error-level issues
     */
    public long getErrorCount() {
        return issues.stream()
                .filter(i -> i.severity() == ValidationIssue.Severity.ERROR)
                .count();
    }

    /**
     * Prints a human-readable summary of this report to standard output.
     */
    public void printSummary() {
        System.out.println("\n=== Validation Report: " + filePath + " ===");
        if (issues.isEmpty()) {
            System.out.println("✓ No issues found.");
        } else {
            issues.forEach(System.out::println);
        }
        System.out.printf("%nTotal methods: %d | Errors: %d%n", totalMethods, getErrorCount());
    }
}
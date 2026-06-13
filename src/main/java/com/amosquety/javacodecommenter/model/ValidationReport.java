package com.amosquety.javacodecommenter.model;

import java.util.List;

/**
 * Aggregates all validation issues found in a source file.
 */
public class ValidationReport {

    private String filePath;
    private List<ValidationIssue> issues;
    private int totalMethods;

    /**
     * Creates a validation report for a file.
     *
     * @param filePath     path to the validated source file
     * @param issues       all issues found in the file
     * @param totalMethods total number of methods examined
     */
    public ValidationReport(String filePath, List<ValidationIssue> issues, int totalMethods) {
        this.filePath = filePath;
        this.issues = issues;
        this.totalMethods = totalMethods;
    }

    /**
     * Returns the path of the validated source file.
     *
     * @return the file path
     */
    public String getFilePath() { return filePath; }

    /**
     * Returns all issues found in the file.
     *
     * @return the validation issues
     */
    public List<ValidationIssue> getIssues() { return issues; }

    /**
     * Returns the total number of methods examined.
     *
     * @return the method count
     */
    public int getTotalMethods() { return totalMethods; }

    /**
     * Counts the issues with {@link ValidationIssue.Severity#ERROR} severity.
     *
     * @return the number of error-level issues
     */
    public long getErrorCount() {
        return issues.stream()
                .filter(i -> i.getSeverity() == ValidationIssue.Severity.ERROR)
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
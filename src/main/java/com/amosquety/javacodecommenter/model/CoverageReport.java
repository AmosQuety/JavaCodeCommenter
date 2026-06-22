package com.amosquety.javacodecommenter.model;

import java.util.Objects;

/**
 * Holds documentation coverage statistics for a file or project.
 */
public record CoverageReport(
        String filePath,
        int totalMethods,
        int documentedMethods
) {

    /**
     * Creates a coverage report.
     *
     * @param filePath          path to the file (or {@code PROJECT} for a summary)
     * @param totalMethods      total number of methods
     * @param documentedMethods number of methods with Javadoc
     */
    public CoverageReport {
        Objects.requireNonNull(filePath, "filePath");
    }

    /**
     * Calculates the documentation coverage percentage.
     *
     * @return the percentage of documented methods; {@code 100.0} when there are no methods
     */
    public double getCoveragePercentage() {
        if (totalMethods == 0) return 100.0;
        return (documentedMethods * 100.0) / totalMethods;
    }

    /**
     * Prints a single coverage line to standard output.
     */
    public void print() {
        System.out.printf("%-50s %d/%d (%.1f%%)%n",
                filePath, documentedMethods, totalMethods, getCoveragePercentage());
    }
}
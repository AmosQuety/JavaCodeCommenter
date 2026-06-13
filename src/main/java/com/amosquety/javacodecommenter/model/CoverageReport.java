package com.amosquety.javacodecommenter.model;

/**
 * Holds documentation coverage statistics for a file or project.
 */
public class CoverageReport {

    private String filePath;
    private int totalMethods;
    private int documentedMethods;

    /**
     * Creates a coverage report.
     *
     * @param filePath          path to the file (or {@code PROJECT} for a summary)
     * @param totalMethods      total number of methods
     * @param documentedMethods number of methods with Javadoc
     */
    public CoverageReport(String filePath, int totalMethods, int documentedMethods) {
        this.filePath = filePath;
        this.totalMethods = totalMethods;
        this.documentedMethods = documentedMethods;
    }

    /**
     * Returns the path of the analyzed file.
     *
     * @return the file path
     */
    public String getFilePath() { return filePath; }

    /**
     * Returns the total number of methods.
     *
     * @return the method count
     */
    public int getTotalMethods() { return totalMethods; }

    /**
     * Returns the number of documented methods.
     *
     * @return the documented method count
     */
    public int getDocumentedMethods() { return documentedMethods; }

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
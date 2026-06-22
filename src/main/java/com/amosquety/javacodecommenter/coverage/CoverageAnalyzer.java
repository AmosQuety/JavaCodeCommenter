package com.amosquety.javacodecommenter.coverage;

import com.amosquety.javacodecommenter.model.CoverageReport;
import com.amosquety.javacodecommenter.model.MethodInfo;

import java.nio.file.Path;
import java.util.List;

/**
 * Analyzes documentation coverage for a set of parsed methods.
 */
public class CoverageAnalyzer {

    /**
     * Calculates documentation coverage for a single file.
     *
     * @param filePath path to the source file
     * @param methods  list of parsed methods from the file
     * @return coverage report for the file
     */
    public CoverageReport analyze(Path filePath, List<MethodInfo> methods) {
        int total = methods.size();
        int documented = (int) methods.stream()
                .filter(MethodInfo::hasExistingJavadoc)
                .count();

        return new CoverageReport(filePath.toString(), total, documented);
    }
}
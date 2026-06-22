package com.amosquety.javacodecommenter.coverage;

import com.amosquety.javacodecommenter.model.CoverageReport;
import com.amosquety.javacodecommenter.model.MethodInfo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests Javadoc coverage calculations.
 */
class CoverageAnalyzerTest {

    /**
     * Verifies partial coverage percentage is calculated correctly.
     */
    @Test
    void analyzeCalculatesCoveragePercentage() {
        MethodInfo documented = new MethodInfo("a", List.of(), List.of(), "void", List.of(), 1);
        documented.setExistingJavadoc("A.");

        MethodInfo undocumented = new MethodInfo("b", List.of(), List.of(), "void", List.of(), 2);

        CoverageReport report = new CoverageAnalyzer().analyze(Path.of("Example.java"), List.of(documented, undocumented));

        assertEquals(2, report.totalMethods());
        assertEquals(1, report.documentedMethods());
        assertEquals(50.0, report.getCoveragePercentage());
    }

    /**
     * Verifies files with no methods are considered fully covered.
     */
    @Test
    void analyzeNoMethodsReturnsFullCoverage() {
        CoverageReport report = new CoverageAnalyzer().analyze(Path.of("Empty.java"), List.of());

        assertEquals(0, report.totalMethods());
        assertEquals(100.0, report.getCoveragePercentage());
    }

    /**
     * Verifies all documented methods produce full coverage.
     */
    @Test
    void analyzeAllDocumentedReturnsFullCoverage() {
        MethodInfo first = new MethodInfo("a", List.of(), List.of(), "void", List.of(), 1);
        MethodInfo second = new MethodInfo("b", List.of(), List.of(), "void", List.of(), 2);

        first.setExistingJavadoc("A.");
        second.setExistingJavadoc("B.");

        CoverageReport report = new CoverageAnalyzer().analyze(Path.of("Example.java"), List.of(first, second));

        assertEquals(100.0, report.getCoveragePercentage());
    }
}
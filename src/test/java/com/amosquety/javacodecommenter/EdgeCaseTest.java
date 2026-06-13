package com.amosquety.javacodecommenter;

import com.amosquety.javacodecommenter.generator.JavadocGenerator;
import com.amosquety.javacodecommenter.model.MethodInfo;
import com.amosquety.javacodecommenter.model.ValidationReport;
import com.amosquety.javacodecommenter.validator.JavadocValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests boundary conditions across generation and validation: methods with no
 * parameters, void return types, and multiple thrown exceptions.
 */
class EdgeCaseTest {

    /**
     * Verifies a no-argument void method generates neither {@code @param} nor
     * {@code @return} tags.
     */
    @Test
    void generateNoParamsVoidReturnOmitsParamAndReturn() {
        MethodInfo method = new MethodInfo("shutdown", List.of(), List.of(), "void", List.of(), 1);

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("Shutdown."));
        assertFalse(javadoc.contains("@param"));
        assertFalse(javadoc.contains("@return"));
    }

    /**
     * Verifies a method declaring multiple exceptions generates one
     * {@code @throws} tag per exception.
     */
    @Test
    void generateMultipleExceptionsProducesThrowsForEach() {
        MethodInfo method = new MethodInfo(
                "connect",
                List.of("host"),
                List.of("String"),
                "void",
                List.of("IOException", "TimeoutException"),
                3
        );

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("@throws IOException"));
        assertTrue(javadoc.contains("@throws TimeoutException"));
    }

    /**
     * Verifies the validator emits a warning for every undocumented exception
     * while keeping the error count at zero when params and return are covered.
     */
    @Test
    void validateMultipleExceptionsWarnsForEachMissingThrows() {
        MethodInfo method = new MethodInfo(
                "connect",
                List.of(),
                List.of(),
                "void",
                List.of("IOException", "TimeoutException"),
                3
        );
        method.setExistingJavadoc("""
                Opens a connection.
                """);

        ValidationReport report = new JavadocValidator().validate("Example.java", List.of(method));

        assertEquals(0, report.getErrorCount());
        assertEquals(2, report.getIssues().size());
    }

    /**
     * Verifies a fully documented no-argument void method produces no issues.
     */
    @Test
    void validateNoParamsVoidReturnIsClean() {
        MethodInfo method = new MethodInfo("shutdown", List.of(), List.of(), "void", List.of(), 1);
        method.setExistingJavadoc("""
                Shuts the service down.
                """);

        ValidationReport report = new JavadocValidator().validate("Example.java", List.of(method));

        assertEquals(0, report.getErrorCount());
        assertTrue(report.getIssues().isEmpty());
    }
}

package com.amosquety.javacodecommenter.validator;

import com.amosquety.javacodecommenter.model.MethodInfo;
import com.amosquety.javacodecommenter.model.ValidationReport;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests Javadoc validation behavior.
 */
class JavadocValidatorTest {

    /**
     * Verifies validator catches missing param, return, and throws tags.
     */
    @Test
    void validateCatchesMissingTags() {
        MethodInfo method = new MethodInfo(
                "load",
                List.of("path"),
                List.of("String"),
                "String",
                List.of("IOException"),
                12
        );
        method.setExistingJavadoc("""
                Loads a file.
                """);

        ValidationReport report = new JavadocValidator().validate(Path.of("Example.java"), List.of(method));

        assertEquals(2, report.getErrorCount());
        assertEquals(3, report.getIssues().size());
    }

    /**
     * Verifies a method without Javadoc reports one error.
     */
    @Test
    void validateCatchesMissingJavadoc() {
        MethodInfo method = new MethodInfo(
                "run",
                List.of(),
                List.of(),
                "void",
                List.of(),
                3
        );

        ValidationReport report = new JavadocValidator().validate(Path.of("Example.java"), List.of(method));

        assertEquals(1, report.getErrorCount());
    }

    /**
     * Verifies validator ignores methods with all tags present.
     */
    @Test
    void validateIgnoresMethodsWithAllTagsPresent() {
        MethodInfo method = new MethodInfo(
                "save",
                List.of("data"),
                List.of("String"),
                "void",
                List.of(),
                7
        );
        method.setExistingJavadoc("""
                Saves data.
                @param data The data to save.
                @return void
                """);

        ValidationReport report = new JavadocValidator().validate(Path.of("Example.java"), List.of(method));

        assertEquals(0, report.getErrorCount());
    }
}
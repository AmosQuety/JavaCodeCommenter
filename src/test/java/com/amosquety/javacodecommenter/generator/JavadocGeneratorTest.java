package com.amosquety.javacodecommenter.generator;

import com.amosquety.javacodecommenter.model.MethodInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Javadoc generation from method signatures.
 */
class JavadocGeneratorTest {

    /**
     * Verifies generated Javadoc includes summary, parameters, return, and throws tags.
     */
    @Test
    void generateIncludesSignatureTags() {
        MethodInfo method = new MethodInfo(
                "calculateDiscount",
                List.of("price", "rate"),
                List.of("double", "double"),
                "double",
                List.of("IllegalArgumentException"),
                10
        );

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("Calculate discount."));
        assertTrue(javadoc.contains("@param price"));
        assertTrue(javadoc.contains("@param rate"));
        assertTrue(javadoc.contains("@return"));
        assertTrue(javadoc.contains("@throws IllegalArgumentException"));
    }

    /**
     * Verifies zero-parameter void methods do not receive param or return tags.
     */
    @Test
    void generateHandlesZeroParamsAndVoidReturn() {
        MethodInfo method = new MethodInfo(
                "reset",
                List.of(),
                List.of(),
                "void",
                List.of(),
                5
        );

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("Reset."));
        assertTrue(!javadoc.contains("@param"));
        assertTrue(!javadoc.contains("@return"));
    }
}
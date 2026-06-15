package com.amosquety.javacodecommenter.generator;

import com.amosquety.javacodecommenter.model.MethodInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Javadoc generation from method signatures, asserting the inferred
 * descriptions produced by {@link JavadocGenerator} and {@link ParamDescriptionInferrer}.
 */
class JavadocGeneratorTest {

    /**
     * Verifies a verb-noun method produces a smart summary that references its
     * parameters and inferred return and throws descriptions.
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

        assertTrue(javadoc.contains("Calculates the discount for the given price and rate."));
        assertTrue(javadoc.contains("@param price the price"));
        assertTrue(javadoc.contains("@param rate the rate"));
        assertTrue(javadoc.contains("@return the double result"));
        assertTrue(javadoc.contains("@throws IllegalArgumentException if any argument is null or invalid"));
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
        assertFalse(javadoc.contains("@param"));
        assertFalse(javadoc.contains("@return"));
    }

    /**
     * Verifies a {@code getXById} name yields a "with the specified ID" summary
     * and that the {@code id} parameter is described as a unique identifier.
     */
    @Test
    void generateInfersByIdSummaryAndIdParam() {
        MethodInfo method = new MethodInfo(
                "getUserById",
                List.of("id"),
                List.of("Long"),
                "User",
                List.of(),
                12
        );

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("Returns the user with the specified ID."));
        assertTrue(javadoc.contains("@param id the unique identifier"));
        assertTrue(javadoc.contains("@return the user result"));
    }

    /**
     * Verifies an {@code isXValid} boolean method produces a "Checks whether ..."
     * summary and a "true if ..., false otherwise" return description.
     */
    @Test
    void generateInfersBooleanSummaryAndReturn() {
        MethodInfo method = new MethodInfo(
                "isEmailValid",
                List.of("email"),
                List.of("String"),
                "boolean",
                List.of(),
                20
        );

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("Checks whether the email is valid."));
        assertTrue(javadoc.contains("@param email the email address"));
        assertTrue(javadoc.contains("@return true if email valid, false otherwise"));
    }

    /**
     * Verifies the exact-name and suffix-pattern dictionaries drive parameter
     * descriptions ahead of any type-based fallback.
     */
    @Test
    void generateInfersParamsFromNameDictionaryAndSuffixes() {
        MethodInfo method = new MethodInfo(
                "search",
                List.of("timeout", "userId", "userName", "errorCount", "imageUrl"),
                List.of("long", "Long", "String", "int", "String"),
                "void",
                List.of(),
                30
        );

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("@param timeout the timeout duration in milliseconds"));
        assertTrue(javadoc.contains("@param userId the unique identifier for the user"));
        assertTrue(javadoc.contains("@param userName the name of the user"));
        assertTrue(javadoc.contains("@param errorCount the number of errors"));
        assertTrue(javadoc.contains("@param imageUrl the URL of the image"));
    }

    /**
     * Verifies generic collection types drive both parameter and return
     * descriptions, including pluralization of the element type.
     */
    @Test
    void generateInfersCollectionAndOptionalDescriptions() {
        MethodInfo method = new MethodInfo(
                "findActiveUsers",
                List.of("roles"),
                List.of("List<Role>"),
                "List<User>",
                List.of(),
                40
        );

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("@param roles the list of roles"));
        assertTrue(javadoc.contains("@return a list of users, or an empty list if none found"));
    }

    /**
     * Verifies an {@code Optional} return type is described as an Optional that
     * may be empty.
     */
    @Test
    void generateInfersOptionalReturn() {
        MethodInfo method = new MethodInfo(
                "findUser",
                List.of("id"),
                List.of("Long"),
                "Optional<User>",
                List.of(),
                45
        );

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("@return an Optional containing the user, or empty if not found"));
    }

    /**
     * Verifies boolean parameters and a {@code String} return are described from
     * the noun part of the method name.
     */
    @Test
    void generateInfersBooleanParamAndStringReturn() {
        MethodInfo method = new MethodInfo(
                "getDisplayName",
                List.of("verbose"),
                List.of("boolean"),
                "String",
                List.of(),
                50
        );

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("@param verbose whether verbose"));
        assertTrue(javadoc.contains("@return the display name"));
    }

    /**
     * Verifies each known exception type yields a tailored {@code @throws}
     * description and unknown types fall back to a method-based phrasing.
     */
    @Test
    void generateInfersThrowsDescriptions() {
        MethodInfo method = new MethodInfo(
                "loadConfig",
                List.of(),
                List.of(),
                "void",
                List.of("IOException", "FileNotFoundException", "TimeoutException"),
                60
        );

        String javadoc = new JavadocGenerator().generate(method);

        assertTrue(javadoc.contains("@throws IOException if an I/O error occurs while processing the file"));
        assertTrue(javadoc.contains("@throws FileNotFoundException if the specified file does not exist"));
        assertTrue(javadoc.contains("@throws TimeoutException if an error occurs during load config"));
    }
}

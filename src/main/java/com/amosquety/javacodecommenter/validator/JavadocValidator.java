package com.amosquety.javacodecommenter.validator;

import com.amosquety.javacodecommenter.model.MethodInfo;
import com.amosquety.javacodecommenter.model.ValidationIssue;
import com.amosquety.javacodecommenter.model.ValidationIssue.Severity;
import com.amosquety.javacodecommenter.model.ValidationReport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates existing Javadoc comments against actual method signatures.
 */
public class JavadocValidator {

    /**
     * Validates all methods in a file and returns a report.
     *
     * @param filePath path to the source file
     * @param methods  list of parsed methods
     * @return validation report containing all found issues
     */
    public ValidationReport validate(Path filePath, List<MethodInfo> methods) {
        List<ValidationIssue> allIssues = new ArrayList<>();

        for (MethodInfo method : methods) {
            allIssues.addAll(validateMethod(method));
        }

        return new ValidationReport(filePath.toString(), allIssues, methods.size());
    }

    /**
     * Validates a single method's Javadoc.
     *
     * @param method the method to validate
     * @return list of issues found
     */
    private List<ValidationIssue> validateMethod(MethodInfo method) {
        List<ValidationIssue> issues = new ArrayList<>();

        // No Javadoc at all
        if (!method.hasExistingJavadoc()) {
            issues.add(new ValidationIssue(
                    method.getName(),
                    method.getLineNumber(),
                    Severity.ERROR,
                    "Missing Javadoc comment"
            ));
            return issues; // no point checking further
        }

        String javadoc = method.getExistingJavadoc().orElse("");

        // Check each parameter is documented
        for (String param : method.getParamNames()) {
            if (!hasTag(javadoc, "@param", param)) {
                issues.add(new ValidationIssue(
                        method.getName(),
                        method.getLineNumber(),
                        Severity.ERROR,
                        "Missing @param tag for: " + param
                ));
            }
        }

        // Check @return exists for non-void methods
        if (!method.getReturnType().equals("void") && !javadoc.contains("@return")) {
            issues.add(new ValidationIssue(
                    method.getName(),
                    method.getLineNumber(),
                    Severity.ERROR,
                    "Missing @return tag (method returns " + method.getReturnType() + ")"
            ));
        }

        // Check @throws for declared exceptions
        for (String ex : method.getThrownExceptions()) {
            if (!hasTag(javadoc, "@throws", ex)) {
                issues.add(new ValidationIssue(
                        method.getName(),
                        method.getLineNumber(),
                        Severity.WARNING,
                        "Missing @throws tag for: " + ex
                ));
            }
        }

        return issues;
    }

    /**
     * Checks whether a Javadoc block documents a tag for the exact given name.
     *
     * <p>Matching is anchored on a word boundary so that, for example,
     * {@code @param id} is not satisfied by {@code @param identifier}: the name
     * must be followed by whitespace or the end of input.
     *
     * @param javadoc the Javadoc content to search
     * @param tag     the Javadoc tag, e.g. {@code @param} or {@code @throws}
     * @param name    the exact parameter or exception name that must follow the tag
     * @return {@code true} if the tag documents exactly {@code name}
     */
    private boolean hasTag(String javadoc, String tag, String name) {
        Pattern pattern = Pattern.compile(Pattern.quote(tag) + "\\s+" + Pattern.quote(name) + "(\\s|$)");
        return pattern.matcher(javadoc).find();
    }
}
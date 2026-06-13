package com.amosquety.javacodecommenter.validator;

import com.amosquety.javacodecommenter.model.MethodInfo;
import com.amosquety.javacodecommenter.model.ValidationIssue;
import com.amosquety.javacodecommenter.model.ValidationIssue.Severity;
import com.amosquety.javacodecommenter.model.ValidationReport;

import java.util.ArrayList;
import java.util.List;

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
    public ValidationReport validate(String filePath, List<MethodInfo> methods) {
        List<ValidationIssue> allIssues = new ArrayList<>();

        for (MethodInfo method : methods) {
            allIssues.addAll(validateMethod(method));
        }

        return new ValidationReport(filePath, allIssues, methods.size());
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

        String javadoc = method.getExistingJavadoc();

        // Check each parameter is documented
        for (String param : method.getParamNames()) {
            if (!javadoc.contains("@param " + param)) {
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
            if (!javadoc.contains("@throws " + ex)) {
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
}
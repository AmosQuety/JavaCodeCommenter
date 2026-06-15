package com.amosquety.javacodecommenter.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds all parsed information about a Java method or constructor.
 */
public class MethodInfo {

    private String name;
    private List<String> paramNames;
    private List<String> paramTypes;
    private String returnType;
    private List<String> thrownExceptions;
    private boolean hasExistingJavadoc;
    private String existingJavadoc;
    private int lineNumber;

    /**
     * Creates method information from a parsed signature.
     *
     * @param name             the method name
     * @param paramNames       the parameter names, in declaration order
     * @param paramTypes       the parameter types, in declaration order
     * @param returnType       the declared return type ({@code void} if none)
     * @param thrownExceptions the declared thrown exception types
     * @param lineNumber       the 1-based line where the method begins
     */
    public MethodInfo(String name, List<String> paramNames, List<String> paramTypes,
                      String returnType, List<String> thrownExceptions, int lineNumber) {
        this.name = Objects.requireNonNull(name, "name");
        this.paramNames = Objects.requireNonNull(paramNames, "paramNames");
        this.paramTypes = Objects.requireNonNull(paramTypes, "paramTypes");
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.thrownExceptions = Objects.requireNonNull(thrownExceptions, "thrownExceptions");
        this.lineNumber = lineNumber;
        this.hasExistingJavadoc = false;
        this.existingJavadoc = null;
    }

    /**
     * Returns the method name.
     *
     * @return the method name
     */
    public String getName() { return name; }

    /**
     * Returns the parameter names in declaration order.
     *
     * @return the parameter names
     */
    public List<String> getParamNames() { return Collections.unmodifiableList(paramNames); }

    /**
     * Returns the parameter types in declaration order.
     *
     * @return the parameter types
     */
    public List<String> getParamTypes() { return Collections.unmodifiableList(paramTypes); }

    /**
     * Returns the declared return type.
     *
     * @return the return type, or {@code void} if the method returns nothing
     */
    public String getReturnType() { return returnType; }

    /**
     * Returns the declared thrown exception types.
     *
     * @return the thrown exception types
     */
    public List<String> getThrownExceptions() { return Collections.unmodifiableList(thrownExceptions); }

    /**
     * Returns the 1-based line where the method begins.
     *
     * @return the line number
     */
    public int getLineNumber() { return lineNumber; }

    /**
     * Indicates whether the method already has a Javadoc comment.
     *
     * @return {@code true} if non-blank Javadoc is present
     */
    public boolean hasExistingJavadoc() { return hasExistingJavadoc; }

    /**
     * Returns the existing Javadoc content, if any.
     *
     * @return an {@link Optional} containing the Javadoc content, or empty if none was set
     */
    public Optional<String> getExistingJavadoc() { return Optional.ofNullable(existingJavadoc); }

    /**
     * Sets the existing Javadoc content and updates the presence flag.
     *
     * @param javadoc the Javadoc content, or {@code null} if none exists
     */
    public void setExistingJavadoc(String javadoc) {
        this.existingJavadoc = javadoc;
        this.hasExistingJavadoc = (javadoc != null && !javadoc.isBlank());
    }
}
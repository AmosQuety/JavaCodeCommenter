package com.amosquety.javacodecommenter.parser;

import com.amosquety.javacodecommenter.model.MethodInfo;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Parses a Java source file and extracts method signatures and existing Javadoc.
 */
public class JavaSourceParser {

    private Path filePath;
    private CompilationUnit compilationUnit;

    /**
     * Parses the given Java source file.
     *
     * @param filePath path to the {@code .java} file to parse
     * @throws IOException if the file cannot be read or parsed
     */
    public JavaSourceParser(Path filePath) throws IOException {
        this.filePath = Objects.requireNonNull(filePath, "filePath");
        this.compilationUnit = StaticJavaParser.parse(filePath);
    }

    /**
     * Extracts all methods from the parsed file.
     *
     * @return list of MethodInfo objects representing each method found
     */
    public List<MethodInfo> extractMethods() {
        List<MethodInfo> methods = new ArrayList<>();

        compilationUnit.findAll(MethodDeclaration.class).forEach(method -> {
            String name = method.getNameAsString();
            int lineNumber = method.getBegin().map(p -> p.line).orElse(0);

            // Extract parameter names and types
            List<String> paramNames = new ArrayList<>();
            List<String> paramTypes = new ArrayList<>();
            for (Parameter param : method.getParameters()) {
                paramNames.add(param.getNameAsString());
                paramTypes.add(param.getTypeAsString());
            }

            // Extract return type
            String returnType = method.getTypeAsString();

            // Extract thrown exceptions
            List<String> exceptions = new ArrayList<>();
            method.getThrownExceptions().forEach(e -> exceptions.add(e.asString()));

            MethodInfo info = new MethodInfo(name, paramNames, paramTypes, returnType, exceptions, lineNumber);

            // Check for existing Javadoc
            method.getComment().ifPresent(comment -> {
                if (comment instanceof JavadocComment) {
                    info.setExistingJavadoc(comment.getContent());
                }
            });

            methods.add(info);
        });

        return methods;
    }

    /**
     * Returns the raw source code of the parsed file.
     *
     * @return source code as a string
     */
    public String getSourceCode() {
        return compilationUnit.toString();
    }

    /**
     * Returns the path of the parsed source file.
     *
     * @return the file path
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * Returns the parsed compilation unit for further inspection or rewriting.
     *
     * @return the compilation unit
     */
    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }
}
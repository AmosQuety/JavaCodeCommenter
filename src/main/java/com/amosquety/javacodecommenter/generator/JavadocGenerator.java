package com.amosquety.javacodecommenter.generator;

import com.amosquety.javacodecommenter.model.MethodInfo;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;

import java.util.List;

/**
 * Generates Javadoc comment strings from parsed method information.
 */
public class JavadocGenerator {

    /**
     * Generates a Javadoc string for a given method.
     *
     * @param method the method to document
     * @return formatted Javadoc string
     */
    public String generate(MethodInfo method) {
        StringBuilder sb = new StringBuilder();

        sb.append("/**\n");
        sb.append(" * ").append(buildSummary(method.getName())).append("\n");
        sb.append(" *\n");

        List<String> names = method.getParamNames();
        List<String> types = method.getParamTypes();
        for (int i = 0; i < names.size(); i++) {
            sb.append(" * @param ").append(names.get(i))
                    .append(" the ").append(humanize(names.get(i)))
                    .append(" (").append(types.get(i)).append(")")
                    .append("\n");
        }

        if (!method.getReturnType().equals("void")) {
            sb.append(" * @return ").append(humanize(method.getName())).append(" result\n");
        }

        for (String ex : method.getThrownExceptions()) {
            sb.append(" * @throws ").append(ex)
                    .append(" if an error occurs\n");
        }

        sb.append(" */");

        return sb.toString();
    }

    /**
     * Inserts generated Javadoc comments into undocumented methods in a compilation unit.
     *
     * @param compilationUnit parsed compilation unit
     * @param methods parsed method metadata
     * @return updated source code
     */
    public String insertMissingJavadocs(CompilationUnit compilationUnit, List<MethodInfo> methods) {
        List<MethodDeclaration> declarations = compilationUnit.findAll(MethodDeclaration.class);

        for (int i = 0; i < declarations.size() && i < methods.size(); i++) {
            MethodDeclaration declaration = declarations.get(i);
            MethodInfo method = methods.get(i);

            if (!method.hasExistingJavadoc()) {
                String generated = generate(method)
                        .replace("/**", "")
                        .replace("*/", "")
                        .lines()
                        .map(line -> line.replaceFirst("^ \\* ?", ""))
                        .reduce((left, right) -> left + System.lineSeparator() + right)
                        .orElse("");

                declaration.setJavadocComment(new JavadocComment(generated.strip()));
            }
        }

        return compilationUnit.toString();
    }

    /**
     * Converts a camelCase method name into a readable summary sentence.
     *
     * @param methodName the camelCase method name
     * @return human-readable summary string
     */
    private String buildSummary(String methodName) {
        String[] words = methodName.split("(?=[A-Z])");
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                summary.append(capitalize(words[i]));
            } else {
                summary.append(" ").append(words[i].toLowerCase());
            }
        }
        summary.append(".");
        return summary.toString();
    }

    /**
     * Converts a camelCase name into lowercase words for descriptions.
     *
     * @param name the camelCase name
     * @return humanized string
     */
    private String humanize(String name) {
        String[] words = name.split("(?=[A-Z])");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                result.append(words[i].toLowerCase());
            } else {
                result.append(" ").append(words[i].toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * Capitalizes a word.
     *
     * @param word input word
     * @return capitalized word
     */
    private String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }
}
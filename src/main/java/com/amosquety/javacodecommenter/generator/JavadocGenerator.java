package com.amosquety.javacodecommenter.generator;

import com.amosquety.javacodecommenter.model.MethodInfo;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates Javadoc comment strings from parsed method information.
 *
 * <p>Method summaries are derived by parsing the camelCase verb-noun structure of
 * the method name; {@code @param}, {@code @return} and {@code @throws} descriptions
 * are delegated to {@link ParamDescriptionInferrer} so this class stays focused on
 * assembling the comment.
 */
public class JavadocGenerator {

    /**
     * Leading verbs mapped to their natural-language summary openings.
     */
    private static final Map<String, String> VERB_PHRASES = Map.ofEntries(
            Map.entry("get", "Returns"),
            Map.entry("set", "Sets"),
            Map.entry("calculate", "Calculates"),
            Map.entry("is", "Checks whether"),
            Map.entry("has", "Checks whether"),
            Map.entry("find", "Finds"),
            Map.entry("validate", "Validates"),
            Map.entry("build", "Creates"),
            Map.entry("create", "Creates"),
            Map.entry("delete", "Removes"),
            Map.entry("parse", "Parses"),
            Map.entry("convert", "Converts"),
            Map.entry("update", "Updates"),
            Map.entry("check", "Checks"),
            Map.entry("load", "Loads"),
            Map.entry("save", "Saves"),
            Map.entry("process", "Processes"));

    /**
     * Verbs whose summaries weave parameters in directly and so should not have a
     * trailing "for the given ..." clause appended.
     */
    private static final Set<String> NO_PARAM_CLAUSE = Set.of("get", "set", "is", "has");

    /**
     * Adjectives recognised when phrasing {@code is}/{@code has} conditions as
     * "the subject is adjective".
     */
    private static final Set<String> STATE_WORDS = Set.of(
            "valid", "empty", "enabled", "active", "present", "null", "available",
            "ready", "set", "expired", "complete", "done", "visible", "blank",
            "closed", "open");

    /**
     * Infers parameter, return and throws descriptions for generated comments.
     */
    private final ParamDescriptionInferrer inferrer = new ParamDescriptionInferrer();

    /**
     * Generates a Javadoc string for a given method.
     *
     * @param method the method to document
     * @return formatted Javadoc string
     */
    public String generate(MethodInfo method) {
        StringBuilder sb = new StringBuilder();

        sb.append("/**\n");
        sb.append(" * ").append(buildSummary(method)).append("\n");
        sb.append(" *\n");

        List<String> names = method.getParamNames();
        List<String> types = method.getParamTypes();
        for (int i = 0; i < names.size(); i++) {
            sb.append(" * @param ").append(names.get(i))
                    .append(" ").append(inferrer.describeParam(names.get(i), types.get(i)))
                    .append("\n");
        }

        if (!method.getReturnType().equals("void")) {
            sb.append(" * @return ")
                    .append(inferrer.describeReturn(method.getName(), method.getReturnType()))
                    .append("\n");
        }

        for (String ex : method.getThrownExceptions()) {
            sb.append(" * @throws ").append(ex).append(" ")
                    .append(inferrer.describeThrows(ex, method.getName()))
                    .append("\n");
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
        Map<String, MethodInfo> methodsByKey = new HashMap<>();
        for (MethodInfo method : methods) {
            methodsByKey.put(methodKey(method.getName(), method.getParamNames().size(), method.getLineNumber()), method);
        }

        for (MethodDeclaration declaration : compilationUnit.findAll(MethodDeclaration.class)) {
            int lineNumber = declaration.getBegin().map(pos -> pos.line).orElse(0);
            String key = methodKey(declaration.getNameAsString(), declaration.getParameters().size(), lineNumber);
            MethodInfo method = methodsByKey.get(key);

            if (method == null || method.hasExistingJavadoc()) {
                continue;
            }

            String generated = generate(method)
                    .replace("/**", "")
                    .replace("*/", "")
                    .lines()
                    .map(line -> line.replaceFirst("^ \\* ?", ""))
                    .reduce((left, right) -> left + System.lineSeparator() + right)
                    .orElse("");

            declaration.setJavadocComment(new JavadocComment(generated.strip()));
        }

        return compilationUnit.toString();
    }

    /**
     * Builds the matching key used to pair a parsed {@link MethodInfo} with its
     * {@link MethodDeclaration} by name, parameter count and declaration line.
     *
     * @param name       the method name
     * @param paramCount the number of declared parameters
     * @param lineNumber the 1-based line where the method begins
     * @return a stable key combining the three identifying attributes
     */
    private String methodKey(String name, int paramCount, int lineNumber) {
        return name + "#" + paramCount + "#" + lineNumber;
    }

    /**
     * Builds a readable summary sentence by parsing the method name's verb-noun
     * structure and, where natural, referencing its parameters.
     *
     * @param method the method to summarise
     * @return a human-readable summary sentence ending with a period
     */
    private String buildSummary(MethodInfo method) {
        String[] words = splitWords(method.getName());
        if (words.length == 0) {
            return capitalize(ParamDescriptionInferrer.humanize(method.getName())) + ".";
        }

        String verb = words[0].toLowerCase();
        String phrase = VERB_PHRASES.get(verb);
        String[] rest = Arrays.copyOfRange(words, 1, words.length);

        if (phrase == null) {
            return capitalize(ParamDescriptionInferrer.humanize(method.getName())) + ".";
        }

        if (verb.equals("is") || verb.equals("has")) {
            return phrase + " " + condition(rest) + ".";
        }

        String byClause = byCriteriaClause(phrase, rest);
        if (byClause != null) {
            return byClause;
        }

        StringBuilder summary = new StringBuilder(phrase);
        String noun = humanizeRange(rest, 0, rest.length);
        if (!noun.isEmpty()) {
            summary.append(" the ").append(noun);
        }
        if (!NO_PARAM_CLAUSE.contains(verb) && !method.getParamNames().isEmpty()) {
            summary.append(" for the given ").append(joinParams(method.getParamNames()));
        }
        summary.append(".");
        return summary.toString();
    }

    /**
     * Phrases an {@code is}/{@code has} condition, preferring "the subject is
     * adjective" when the name ends in a recognised state word.
     *
     * @param rest the method-name words following the verb
     * @return the condition clause, without the leading "Checks whether"
     */
    private String condition(String[] rest) {
        if (rest.length == 0) {
            return "this is set";
        }
        String last = rest[rest.length - 1].toLowerCase();
        if (STATE_WORDS.contains(last)) {
            if (rest.length == 1) {
                return "this is " + last;
            }
            return "the " + humanizeRange(rest, 0, rest.length - 1) + " is " + last;
        }
        return "the " + humanizeRange(rest, 0, rest.length);
    }

    /**
     * Builds a "... with the specified ..." clause for {@code findXByY} style
     * names, or {@code null} if the words contain no {@code By} separator.
     *
     * @param phrase the resolved verb phrase, e.g. "Returns"
     * @param rest   the method-name words following the verb
     * @return the full summary sentence, or {@code null} if not a {@code By} name
     */
    private String byCriteriaClause(String phrase, String[] rest) {
        int byIndex = -1;
        for (int i = 0; i < rest.length; i++) {
            if (rest[i].equalsIgnoreCase("By")) {
                byIndex = i;
                break;
            }
        }
        if (byIndex <= 0 || byIndex >= rest.length - 1) {
            return null;
        }

        String subject = humanizeRange(rest, 0, byIndex);
        String criteria;
        if (rest.length - (byIndex + 1) == 1 && rest[byIndex + 1].equalsIgnoreCase("Id")) {
            criteria = "the specified ID";
        } else {
            criteria = "the specified " + humanizeRange(rest, byIndex + 1, rest.length);
        }
        return phrase + " the " + subject + " with " + criteria + ".";
    }

    /**
     * Joins parameter names into a readable list such as "price and rate" or
     * "a, b and c".
     *
     * @param params the parameter names
     * @return the joined, humanized list
     */
    private String joinParams(List<String> params) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            String name = ParamDescriptionInferrer.humanize(params.get(i));
            if (i == 0) {
                result.append(name);
            } else if (i == params.size() - 1) {
                result.append(" and ").append(name);
            } else {
                result.append(", ").append(name);
            }
        }
        return result.toString();
    }

    /**
     * Splits a camelCase name into its words, discarding empty segments.
     *
     * @param name the camelCase name
     * @return the individual words, preserving their original capitalization
     */
    private String[] splitWords(String name) {
        return Arrays.stream(name.split("(?=[A-Z])"))
                .filter(word -> !word.isEmpty())
                .toArray(String[]::new);
    }

    /**
     * Humanizes a range of camelCase words into space-separated lowercase text.
     *
     * @param words the word array
     * @param from  the inclusive start index
     * @param to    the exclusive end index
     * @return the humanized text for the range
     */
    private String humanizeRange(String[] words, int from, int to) {
        StringBuilder result = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(words[i].toLowerCase());
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

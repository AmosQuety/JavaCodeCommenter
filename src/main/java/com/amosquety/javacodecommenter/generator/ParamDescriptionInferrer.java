package com.amosquety.javacodecommenter.generator;

import java.util.Map;
import java.util.Set;

/**
 * Infers human-readable descriptions for {@code @param}, {@code @return} and
 * {@code @throws} tags from method signatures.
 *
 * <p>Inference is rule-based and fully deterministic. Parameter descriptions are
 * resolved in priority order: an exact-name dictionary, then a suffix-pattern
 * dictionary, then the declared type, and finally a humanized last-resort
 * fallback. The goal is to never emit a meaningless placeholder such as
 * {@code "Description of parameter"}.
 */
public class ParamDescriptionInferrer {

    /**
     * Leading verbs recognised when extracting the noun part of a method name.
     */
    private static final Set<String> VERBS = Set.of(
            "calculate", "get", "set", "find", "validate", "parse", "build",
            "create", "delete", "update", "check", "is", "has", "load", "save",
            "convert", "process");

    /**
     * Exact parameter-name matches mapped to their descriptions.
     */
    private static final Map<String, String> EXACT_NAMES = Map.ofEntries(
            Map.entry("id", "the unique identifier"),
            Map.entry("name", "the name"),
            Map.entry("email", "the email address"),
            Map.entry("url", "the URL"),
            Map.entry("path", "the file path"),
            Map.entry("file", "the file to process"),
            Map.entry("dir", "the directory path"),
            Map.entry("directory", "the directory path"),
            Map.entry("config", "the configuration settings"),
            Map.entry("timeout", "the timeout duration in milliseconds"),
            Map.entry("limit", "the maximum number of results"),
            Map.entry("offset", "the starting offset for pagination"),
            Map.entry("page", "the page number"),
            Map.entry("size", "the number of items"),
            Map.entry("count", "the number of items"),
            Map.entry("query", "the search query"),
            Map.entry("key", "the lookup key"),
            Map.entry("value", "the value to set"),
            Map.entry("message", "the message content"),
            Map.entry("error", "the error that occurred"),
            Map.entry("input", "the input to process"),
            Map.entry("output", "the output destination"),
            Map.entry("source", "the source to read from"),
            Map.entry("target", "the target to write to"),
            Map.entry("index", "the zero-based index"),
            Map.entry("flag", "whether this is enabled"),
            Map.entry("enabled", "whether this is enabled"),
            Map.entry("active", "whether this is enabled"));

    /**
     * Exception simple names mapped to their {@code @throws} descriptions.
     */
    private static final Map<String, String> EXCEPTIONS = Map.of(
            "IllegalArgumentException", "if any argument is null or invalid",
            "IllegalStateException", "if the object is in an invalid state",
            "IOException", "if an I/O error occurs while processing the file",
            "FileNotFoundException", "if the specified file does not exist",
            "NullPointerException", "if a required parameter is null",
            "UnsupportedOperationException", "if this operation is not supported");

    /**
     * Infers a description for a single parameter from its name and type.
     *
     * @param name the parameter name
     * @param type the declared parameter type
     * @return a human-readable description, never a generic placeholder
     */
    public String describeParam(String name, String type) {
        String exact = EXACT_NAMES.get(name.toLowerCase());
        if (exact != null) {
            return exact;
        }

        String suffixMatch = matchSuffix(name);
        if (suffixMatch != null) {
            return suffixMatch;
        }

        String raw = simpleRaw(type);
        if (raw.equalsIgnoreCase("boolean")) {
            return "whether " + humanize(name);
        }
        if (raw.equals("List")) {
            return "the list of " + listWord(type) + "s";
        }

        return "the " + humanize(name);
    }

    /**
     * Infers a description for the {@code @return} tag.
     *
     * @param methodName the method name
     * @param returnType the declared return type ({@code void} returns an empty string)
     * @return a human-readable return description, or an empty string for {@code void}
     */
    public String describeReturn(String methodName, String returnType) {
        String raw = simpleRaw(returnType);
        if (raw.equals("void")) {
            return "";
        }
        if (raw.equalsIgnoreCase("boolean")) {
            return "true if " + stripBooleanPrefix(methodName) + ", false otherwise";
        }
        if (raw.equals("List")) {
            return "a list of " + listWord(returnType) + "s, or an empty list if none found";
        }
        if (raw.equals("Optional")) {
            String inner = genericInner(returnType);
            String word = inner == null ? "value" : humanize(inner);
            return "an Optional containing the " + word + ", or empty if not found";
        }
        if (raw.equals("String")) {
            return "the " + nounPart(methodName);
        }
        return "the " + humanize(raw) + " result";
    }

    /**
     * Infers a description for a {@code @throws} tag.
     *
     * @param exceptionType the declared exception type (simple or qualified name)
     * @param methodName    the method name, used for the generic fallback
     * @return a human-readable throws description
     */
    public String describeThrows(String exceptionType, String methodName) {
        String description = EXCEPTIONS.get(simpleRaw(exceptionType));
        if (description != null) {
            return description;
        }
        return "if an error occurs during " + humanize(methodName);
    }

    /**
     * Converts a camelCase identifier into space-separated lowercase words.
     *
     * @param name the camelCase identifier
     * @return the humanized form, or an empty string if {@code name} is blank
     */
    public static String humanize(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String[] words = name.split("(?=[A-Z])");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(word.toLowerCase());
        }
        return result.toString();
    }

    /**
     * Attempts to match a parameter name against the suffix-pattern dictionary.
     *
     * @param name the parameter name
     * @return the inferred description, or {@code null} if no suffix matches
     */
    private String matchSuffix(String name) {
        if (endsWithSegment(name, "Id")) {
            return "the unique identifier for the " + humanize(prefix(name, 2));
        }
        if (endsWithSegment(name, "Name")) {
            return "the name of the " + humanize(prefix(name, 4));
        }
        if (endsWithSegment(name, "List")) {
            return "the collection of " + humanize(prefix(name, 4)) + "s";
        }
        if (endsWithSegment(name, "Map")) {
            return "the collection of " + humanize(prefix(name, 3)) + "s";
        }
        if (endsWithSegment(name, "Set")) {
            return "the collection of " + humanize(prefix(name, 3)) + "s";
        }
        if (endsWithSegment(name, "Path")) {
            return "the file path to the " + humanize(prefix(name, 4));
        }
        if (endsWithSegment(name, "Url")) {
            return "the URL of the " + humanize(prefix(name, 3));
        }
        if (endsWithSegment(name, "Count")) {
            return "the number of " + humanize(prefix(name, 5)) + "s";
        }
        if (endsWithSegment(name, "Type")) {
            return "the type of " + humanize(prefix(name, 4));
        }
        return null;
    }

    /**
     * Indicates whether a name ends with the given capitalized segment and has a
     * non-empty prefix before it.
     *
     * @param name    the parameter name
     * @param segment the capitalized suffix segment to test
     * @return {@code true} if {@code name} ends with {@code segment} and is longer
     */
    private boolean endsWithSegment(String name, String segment) {
        return name.length() > segment.length() && name.endsWith(segment);
    }

    /**
     * Returns the portion of a name preceding a suffix of the given length.
     *
     * @param name         the parameter name
     * @param suffixLength the length of the trailing suffix to remove
     * @return the prefix portion of the name
     */
    private String prefix(String name, int suffixLength) {
        return name.substring(0, name.length() - suffixLength);
    }

    /**
     * Derives the singular word describing the element type of a {@code List}.
     *
     * @param type the declared list type, e.g. {@code List<User>}
     * @return the humanized element word, defaulting to {@code "item"}
     */
    private String listWord(String type) {
        String inner = genericInner(type);
        return inner == null ? "item" : humanize(inner);
    }

    /**
     * Strips a leading {@code is}/{@code has} prefix from a boolean method name
     * and humanizes the remainder.
     *
     * @param methodName the boolean method name
     * @return the humanized condition the method tests
     */
    private String stripBooleanPrefix(String methodName) {
        for (String prefix : new String[]{"is", "has"}) {
            if (methodName.length() > prefix.length()
                    && methodName.startsWith(prefix)
                    && Character.isUpperCase(methodName.charAt(prefix.length()))) {
                return humanize(methodName.substring(prefix.length()));
            }
        }
        return humanize(methodName);
    }

    /**
     * Extracts and humanizes the noun part of a method name by dropping a known
     * leading verb.
     *
     * @param methodName the method name
     * @return the humanized noun part, or the whole name if no verb is present
     */
    private String nounPart(String methodName) {
        String[] words = methodName.split("(?=[A-Z])");
        if (words.length > 1 && VERBS.contains(words[0].toLowerCase())) {
            StringBuilder result = new StringBuilder();
            for (int i = 1; i < words.length; i++) {
                if (result.length() > 0) {
                    result.append(' ');
                }
                result.append(words[i].toLowerCase());
            }
            return result.toString();
        }
        return humanize(methodName);
    }

    /**
     * Reduces a possibly-qualified, possibly-generic type to its simple raw name.
     *
     * @param type the declared type
     * @return the simple type name without package or type arguments
     */
    private static String simpleRaw(String type) {
        if (type == null) {
            return "";
        }
        String trimmed = type.trim();
        int angle = trimmed.indexOf('<');
        if (angle >= 0) {
            trimmed = trimmed.substring(0, angle);
        }
        int dot = trimmed.lastIndexOf('.');
        if (dot >= 0) {
            trimmed = trimmed.substring(dot + 1);
        }
        return trimmed;
    }

    /**
     * Extracts the simple name of the first type argument of a generic type.
     *
     * @param type the declared type, e.g. {@code List<User>}
     * @return the simple inner type name, or {@code null} if not generic
     */
    private static String genericInner(String type) {
        int open = type.indexOf('<');
        int close = type.lastIndexOf('>');
        if (open < 0 || close <= open) {
            return null;
        }
        String inner = type.substring(open + 1, close).trim();
        int comma = inner.indexOf(',');
        if (comma >= 0) {
            inner = inner.substring(0, comma).trim();
        }
        return simpleRaw(inner);
    }
}

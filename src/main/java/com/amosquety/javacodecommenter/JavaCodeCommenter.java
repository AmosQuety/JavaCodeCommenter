package com.amosquety.javacodecommenter;

import com.amosquety.javacodecommenter.coverage.CoverageAnalyzer;
import com.amosquety.javacodecommenter.generator.JavadocGenerator;
import com.amosquety.javacodecommenter.model.CoverageReport;
import com.amosquety.javacodecommenter.model.MethodInfo;
import com.amosquety.javacodecommenter.model.ValidationIssue;
import com.amosquety.javacodecommenter.model.ValidationReport;
import com.amosquety.javacodecommenter.parser.JavaSourceParser;
import com.amosquety.javacodecommenter.validator.JavadocValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Main entry point for the JavaCodeCommenter library.
 * Orchestrates parsing, generation, validation, and coverage analysis.
 */
public class JavaCodeCommenter {

    private String filePath;
    private JavaSourceParser parser;
    private List<MethodInfo> methods;

    /**
     * Loads a Java source file for processing.
     *
     * @param filePath path to the .java file
     * @return this instance for chaining
     * @throws IOException if the file cannot be read
     */
    public JavaCodeCommenter fromFile(String filePath) throws IOException {
        this.filePath = filePath;
        this.parser = new JavaSourceParser(filePath);
        this.methods = parser.extractMethods();
        return this;
    }

    /**
     * Resolves a Java file or recursively discovers Java files in a directory.
     *
     * @param path file or directory path
     * @return sorted list of Java source files
     * @throws IOException if files cannot be scanned
     */
    public List<Path> resolveJavaFiles(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            if (!path.toString().endsWith(".java")) {
                throw new IOException("Expected a .java file: " + path);
            }
            return List.of(path);
        }

        if (!Files.isDirectory(path)) {
            throw new IOException("Path does not exist: " + path);
        }

        try (var stream = Files.walk(path)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    /**
     * Generates Javadoc for all undocumented methods and optionally writes changes in-place.
     *
     * @param inPlace whether to update the source file directly
     * @return number of generated Javadoc comments
     * @throws IOException if source cannot be written
     */
    public int generate(boolean inPlace) throws IOException {
        JavadocGenerator generator = new JavadocGenerator();

        System.out.println("\n=== Generating Javadoc: " + filePath + " ===\n");

        int generated = 0;
        for (MethodInfo method : methods) {
            if (!method.hasExistingJavadoc()) {
                System.out.println("// Line " + method.getLineNumber() + " - " + method.getName() + "()");
                System.out.println(generator.generate(method));
                System.out.println();
                generated++;
            }
        }

        if (generated == 0) {
            System.out.println("✓ All methods already have Javadoc.");
            return 0;
        }

        if (inPlace) {
            String updatedSource = generator.insertMissingJavadocs(parser.getCompilationUnit(), methods);
            Files.writeString(Path.of(filePath), updatedSource);
            System.out.println("Updated source file in-place.");
        }

        System.out.println("Generated Javadoc for " + generated + " method(s).");
        return generated;
    }

    /**
     * Generates Javadoc for all methods that are missing documentation.
     * Prints the generated Javadoc to the console.
     */
    public void generate() {
        try {
            generate(false);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate Javadoc for " + filePath, e);
        }
    }

    /**
     * Validates existing Javadoc against actual method signatures.
     *
     * @return validation report with all found issues
     */
    public ValidationReport validate() {
        JavadocValidator validator = new JavadocValidator();
        ValidationReport report = validator.validate(filePath, methods);
        report.printSummary();
        return report;
    }

    /**
     * Validates all provided Java files.
     *
     * @param files Java files to validate
     * @return validation reports
     * @throws IOException if files cannot be read
     */
    public List<ValidationReport> validateAll(List<Path> files) throws IOException {
        JavadocValidator validator = new JavadocValidator();

        return files.stream()
                .map(file -> {
                    try {
                        JavaCodeCommenter commenter = new JavaCodeCommenter().fromFile(file.toString());
                        return validator.validate(file.toString(), commenter.methods);
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to validate " + file, e);
                    }
                })
                .toList();
    }

    /**
     * Prints validation reports in the requested format.
     *
     * @param reports validation reports
     * @param format output format
     */
    public void printValidationReports(List<ValidationReport> reports, String format) {
        switch (format.toLowerCase()) {
            case "json" -> printValidationJson(reports);
            case "markdown" -> printValidationMarkdown(reports);
            case "text" -> {
                reports.forEach(ValidationReport::printSummary);
                long totalMethods = reports.stream().mapToLong(ValidationReport::getTotalMethods).sum();
                long totalErrors = reports.stream().mapToLong(ValidationReport::getErrorCount).sum();
                System.out.printf("%nProject summary: files=%d | methods=%d | errors=%d%n",
                        reports.size(),
                        totalMethods,
                        totalErrors);
            }
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    /**
     * Analyzes documentation coverage for the loaded file.
     *
     * @return coverage report with percentage and counts
     */
    public CoverageReport coverage() {
        CoverageAnalyzer analyzer = new CoverageAnalyzer();
        CoverageReport report = analyzer.analyze(filePath, methods);
        report.print();
        return report;
    }

    /**
     * Analyzes documentation coverage for all provided Java files.
     *
     * @param files Java files to analyze
     * @return coverage reports
     * @throws IOException if files cannot be read
     */
    public List<CoverageReport> coverageAll(List<Path> files) throws IOException {
        CoverageAnalyzer analyzer = new CoverageAnalyzer();

        return files.stream()
                .map(file -> {
                    try {
                        JavaCodeCommenter commenter = new JavaCodeCommenter().fromFile(file.toString());
                        return analyzer.analyze(file.toString(), commenter.methods);
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to analyze coverage for " + file, e);
                    }
                })
                .toList();
    }

    /**
     * Prints coverage reports in the requested format.
     *
     * @param reports coverage reports
     * @param format output format
     */
    public void printCoverageReports(List<CoverageReport> reports, String format) {
        switch (format.toLowerCase()) {
            case "json" -> printCoverageJson(reports);
            case "markdown" -> printCoverageMarkdown(reports);
            case "text" -> {
                reports.forEach(CoverageReport::print);

                int totalMethods = reports.stream().mapToInt(CoverageReport::getTotalMethods).sum();
                int documentedMethods = reports.stream().mapToInt(CoverageReport::getDocumentedMethods).sum();
                CoverageReport project = new CoverageReport("PROJECT", totalMethods, documentedMethods);

                System.out.printf("%nProject summary: %d/%d documented (%.1f%%)%n",
                        documentedMethods,
                        totalMethods,
                        project.getCoveragePercentage());
            }
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    /**
     * Prints validation reports as JSON.
     *
     * @param reports validation reports
     */
    private void printValidationJson(List<ValidationReport> reports) {
        StringBuilder json = new StringBuilder();
        json.append("{\"reports\":[");

        for (int i = 0; i < reports.size(); i++) {
            ValidationReport report = reports.get(i);
            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"filePath\":\"").append(escapeJson(report.getFilePath())).append("\",")
                    .append("\"totalMethods\":").append(report.getTotalMethods()).append(",")
                    .append("\"errorCount\":").append(report.getErrorCount()).append(",")
                    .append("\"issues\":[");

            List<ValidationIssue> issues = report.getIssues();
            for (int j = 0; j < issues.size(); j++) {
                ValidationIssue issue = issues.get(j);
                if (j > 0) {
                    json.append(",");
                }

                json.append("{")
                        .append("\"methodName\":\"").append(escapeJson(issue.getMethodName())).append("\",")
                        .append("\"lineNumber\":").append(issue.getLineNumber()).append(",")
                        .append("\"severity\":\"").append(issue.getSeverity()).append("\",")
                        .append("\"message\":\"").append(escapeJson(issue.getMessage())).append("\"")
                        .append("}");
            }

            json.append("]}");
        }

        json.append("]}");
        System.out.println(json);
    }

    /**
     * Prints coverage reports as JSON.
     *
     * @param reports coverage reports
     */
    private void printCoverageJson(List<CoverageReport> reports) {
        StringBuilder json = new StringBuilder();
        json.append("{\"reports\":[");

        for (int i = 0; i < reports.size(); i++) {
            CoverageReport report = reports.get(i);
            if (i > 0) {
                json.append(",");
            }

            json.append("{")
                    .append("\"filePath\":\"").append(escapeJson(report.getFilePath())).append("\",")
                    .append("\"totalMethods\":").append(report.getTotalMethods()).append(",")
                    .append("\"documentedMethods\":").append(report.getDocumentedMethods()).append(",")
                    .append("\"coveragePercentage\":").append(String.format("%.1f", report.getCoveragePercentage()))
                    .append("}");
        }

        json.append("]}");
        System.out.println(json);
    }

    /**
     * Prints validation reports as Markdown.
     *
     * @param reports validation reports
     */
    private void printValidationMarkdown(List<ValidationReport> reports) {
        System.out.println("# JavaCodeCommenter Validation Report");
        System.out.println();
        System.out.println("| File | Method | Line | Severity | Message |");
        System.out.println("|---|---:|---:|---|---|");

        for (ValidationReport report : reports) {
            if (report.getIssues().isEmpty()) {
                System.out.printf("| `%s` | - | - | OK | No issues found |%n", report.getFilePath());
            } else {
                for (ValidationIssue issue : report.getIssues()) {
                    System.out.printf("| `%s` | `%s` | %d | %s | %s |%n",
                            report.getFilePath(),
                            issue.getMethodName(),
                            issue.getLineNumber(),
                            issue.getSeverity(),
                            issue.getMessage());
                }
            }
        }
    }

    /**
     * Prints coverage reports as Markdown.
     *
     * @param reports coverage reports
     */
    private void printCoverageMarkdown(List<CoverageReport> reports) {
        System.out.println("# JavaCodeCommenter Coverage Report");
        System.out.println();
        System.out.println("| File | Documented | Total | Coverage |");
        System.out.println("|---|---:|---:|---:|");

        for (CoverageReport report : reports) {
            System.out.printf("| `%s` | %d | %d | %.1f%% |%n",
                    report.getFilePath(),
                    report.getDocumentedMethods(),
                    report.getTotalMethods(),
                    report.getCoveragePercentage());
        }
    }

    /**
     * Escapes a value for JSON output.
     *
     * @param value raw value
     * @return JSON-safe value
     */
    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
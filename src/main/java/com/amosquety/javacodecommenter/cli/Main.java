package com.amosquety.javacodecommenter.cli;

import com.amosquety.javacodecommenter.JavaCodeCommenter;
import com.amosquety.javacodecommenter.model.CoverageReport;
import com.amosquety.javacodecommenter.model.ValidationReport;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * CLI entry point for JavaCodeCommenter.
 */
@Command(
        name = "javacc",
        mixinStandardHelpOptions = true,
        version = "JavaCodeCommenter 1.0.0",
        description = "Javadoc generator, validator, and coverage reporter for Java projects.",
        subcommands = {
                Main.GenerateCommand.class,
                Main.ValidateCommand.class,
                Main.CoverageCommand.class
        }
)
public class Main implements Callable<Integer> {

    /**
     * Runs the JavaCodeCommenter CLI.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Displays help when no subcommand is provided.
     *
     * @return process exit code
     */
    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    /**
     * Generates missing Javadoc comments.
     */
    @Command(name = "generate", description = "Generate missing Javadoc comments.")
    static class GenerateCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Java source file or directory to process.")
        private Path path;

        @Option(names = {"-i", "--in-place"}, description = "Write generated Javadoc directly into source files.")
        private boolean inPlace;

        /**
         * Executes Javadoc generation.
         *
         * @return process exit code
         * @throws Exception if processing fails
         */
        @Override
        public Integer call() throws Exception {
            JavaCodeCommenter commenter = new JavaCodeCommenter();
            List<Path> files = commenter.resolveJavaFiles(path);

            int totalGenerated = 0;
            for (Path file : files) {
                totalGenerated += commenter.fromFile(file).generate(inPlace);
            }

            System.out.printf("%nProject summary: generated Javadoc for %d method(s) across %d file(s).%n",
                    totalGenerated,
                    files.size());

            return 0;
        }
    }

    /**
     * Validates existing Javadoc comments.
     */
    @Command(name = "validate", description = "Validate existing Javadoc comments.")
    static class ValidateCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Java source file or directory to process.")
        private Path path;

        @Option(names = "--format", description = "Export format: text, json, markdown.", defaultValue = "text")
        private String format;

        /**
         * Executes Javadoc validation.
         *
         * @return process exit code, 1 when validation errors are found
         * @throws Exception if processing fails
         */
        @Override
        public Integer call() throws Exception {
            JavaCodeCommenter commenter = new JavaCodeCommenter();
            List<Path> files = commenter.resolveJavaFiles(path);

            List<ValidationReport> reports = commenter.validateAll(files);
            commenter.printValidationReports(reports, format);

            long errors = reports.stream()
                    .mapToLong(ValidationReport::getErrorCount)
                    .sum();

            return errors > 0 ? 1 : 0;
        }
    }

    /**
     * Reports Javadoc coverage.
     */
    @Command(name = "coverage", description = "Show Javadoc coverage.")
    static class CoverageCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Java source file or directory to process.")
        private Path path;

        @Option(names = "--format", description = "Export format: text, json, markdown.", defaultValue = "text")
        private String format;

        /**
         * Executes coverage analysis.
         *
         * @return process exit code
         * @throws Exception if processing fails
         */
        @Override
        public Integer call() throws Exception {
            JavaCodeCommenter commenter = new JavaCodeCommenter();
            List<Path> files = commenter.resolveJavaFiles(path);

            List<CoverageReport> reports = commenter.coverageAll(files);
            commenter.printCoverageReports(reports, format);

            return 0;
        }
    }
}
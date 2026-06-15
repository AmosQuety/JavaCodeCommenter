# JavaCodeCommenter

JavaCodeCommenter is a developer tool for generating, validating, and reporting Javadoc coverage for Java source files and projects.

It is the Java equivalent of [PyCodeCommenter](https://github.com/AmosQuety/PyCodeCommenter), designed for Java teams that want lightweight, automated Javadoc quality checks in local development and CI/CD pipelines.

It is fully deterministic and rule-based — no AI APIs, no network calls. Every result is reproducible.

## Why JavaCodeCommenter Exists

Good documentation makes Java code easier to maintain, review, and onboard into. However, keeping Javadocs aligned with method signatures is repetitive and easy to forget. Over time documentation drifts: parameters get renamed, return types change, new exceptions are thrown — and the Javadoc silently goes stale.

JavaCodeCommenter helps by:

- generating starter Javadocs from real method signatures
- validating existing Javadocs against parameters, return types, and thrown exceptions
- reporting Javadoc coverage for files or full projects
- failing CI when validation errors are found

## Features

- **Javadoc generation** — produces a Javadoc skeleton from each method's real signature, including `@param`, `@return`, and `@throws` tags.
- **In-place write-back** — with `-i`/`--in-place`, generated Javadoc is inserted directly into the source file; without it, output is printed to the console only.
- **Signature validation** — flags missing `@param` tags (error), missing `@return` on non-void methods (error), and missing `@throws` for declared exceptions (warning).
- **Coverage reporting** — reports the percentage of documented methods per file and across an entire project.
- **Directory scanning** — every command accepts a file or a directory; directories are scanned recursively for `.java` files, with per-file output and a project-wide summary.
- **Export formats** — `validate` and `coverage` support `--format text` (default), `--format json`, and `--format markdown`.
- **CI-friendly exit codes** — `validate` exits `1` when any error-severity issues are found; `generate` and `coverage` exit `0` on success.

## Installation

Build a runnable fat JAR from source (requires JDK 17 and Maven):

```bash
git clone https://github.com/AmosQuety/JavaCodeCommenter.git
cd JavaCodeCommenter
mvn clean package
```

The shaded JAR is written to `target/javacodecommenter-1.0.0.jar`. Run it with:

```bash
java -jar target/javacodecommenter-1.0.0.jar --help
```

For convenience you can alias the invocation as `javacc`:

```bash
alias javacc='java -jar /path/to/target/javacodecommenter-1.0.0.jar'
```

## CLI Usage

```bash
# Generate missing Javadoc and print it to the console
javacc generate src/main/java/com/example/Service.java

# Generate and write the Javadoc back into the file
javacc generate src/main/java -i

# Validate a file or directory against real signatures
javacc validate src/main/java

# Validate and export results
javacc validate src/main/java --format json
javacc validate src/main/java --format markdown

# Report coverage for a project
javacc coverage src/main/java
javacc coverage src/main/java --format markdown
```

Commands:

| Command    | Description                                          |
|------------|------------------------------------------------------|
| `generate` | Generate missing Javadoc comments (use `-i` to write back). |
| `validate` | Validate existing Javadoc against signatures. Exits `1` on errors. |
| `coverage` | Show Javadoc coverage percentage.                    |

## Library Usage

JavaCodeCommenter can also be used programmatically through the `JavaCodeCommenter` orchestrator class.

```java
import com.amosquety.javacodecommenter.JavaCodeCommenter;
import com.amosquety.javacodecommenter.model.CoverageReport;
import com.amosquety.javacodecommenter.model.ValidationReport;

import java.nio.file.Path;
import java.util.List;

public class Example {
    public static void main(String[] args) throws Exception {
        JavaCodeCommenter commenter = new JavaCodeCommenter();

        // Generate Javadoc for a single file and write it back in-place
        commenter.fromFile("src/main/java/com/example/Service.java")
                 .generate(true);

        // Validate every Java file under a directory
        List<Path> files = commenter.resolveJavaFiles(Path.of("src/main/java"));
        List<ValidationReport> reports = commenter.validateAll(files);
        commenter.printValidationReports(reports, "json");

        long errors = reports.stream().mapToLong(ValidationReport::getErrorCount).sum();
        if (errors > 0) {
            System.exit(1); // enforce documentation quality in CI
        }

        // Analyze coverage and enforce a threshold
        List<CoverageReport> coverage = commenter.coverageAll(files);
        commenter.printCoverageReports(coverage, "text");
    }
}
```

## Roadmap

- Configurable validation rules (treat missing `@throws` as error, ignore patterns).
- Class- and field-level Javadoc validation, not just methods.
- Pre-commit hook and GitHub Actions integration examples.
- Maven and Gradle plugins for build-time enforcement.
- HTML coverage report export.

## Creator

Built by **Nabasa Amos** (AmosQuety), author of the Python equivalent, [PyCodeCommenter](https://pypi.org/project/pycodecommenter/).

## License

MIT License.

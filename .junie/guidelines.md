# JavaCodeCommenter — Project Guidelines

## What This Project Is
JavaCodeCommenter is a Java developer tool that generates, validates, and reports
Javadoc coverage for Java source files. It is the Java equivalent of PyCodeCommenter,
a published Python package by the same author (Nabasa Amos / AmosQuety).

## Reference Links
- Python version (PyPI): https://pypi.org/project/pycodecommenter/
- Python version (GitHub): https://github.com/AmosQuety/PyCodeCommenter
- Python version (Docs): https://amosquety.github.io/PyCodeCommenter/

## Project Coordinates
- Group ID: com.amosquety
- Artifact ID: javacodecommenter
- Version: 1.0.0
- Main class: com.amosquety.javacodecommenter.cli.Main
- Java version: 21 (LTS)
- Build tool: Maven

## Dependencies
- com.github.javaparser:javaparser-core:3.26.4 (AST parsing)
- info.picocli:picocli:4.7.6 (CLI commands)
- org.junit.jupiter:junit-jupiter:5.10.2 (testing)
- maven-shade-plugin:3.5.2 (fat JAR packaging)

## Package Structure
com.amosquety.javacodecommenter
├── model/
│   ├── MethodInfo.java          — parsed method data (name, params, return, throws)
│   ├── ValidationIssue.java     — single validation problem (severity + message)
│   ├── ValidationReport.java    — all issues for a file
│   └── CoverageReport.java      — documented vs total method counts
├── parser/
│   └── JavaSourceParser.java    — JavaParser AST-based source reader
├── generator/
│   └── JavadocGenerator.java    — builds Javadoc strings from MethodInfo
├── validator/
│   └── JavadocValidator.java    — checks existing Javadoc vs real signatures
├── coverage/
│   └── CoverageAnalyzer.java    — calculates coverage percentage
├── cli/
│   └── Main.java                — CLI entry point (generate/validate/coverage)
└── JavaCodeCommenter.java       — orchestrator class (public API)

## CLI Commands
javacc generate <file|dir>    — generate missing Javadoc
javacc validate <file|dir>    — validate existing Javadoc against signatures
javacc coverage <file|dir>    — show documentation coverage percentage

## What v1.0.0 Must Deliver

### 1. Write-back (in-place generation)
- generate command must insert Javadoc directly into the source file
- Add -i / --in-place flag to CLI
- Without -i, print to console only (current behaviour, keep it)

### 2. Directory scanning
- All three commands must accept a directory path
- Recursively find all .java files and process each
- Print per-file output then a project-wide summary

### 3. Export formats
- validate and coverage commands support --format json and --format markdown
- Default format is console output

### 4. Unit tests
- GeneratorTest: Javadoc generation from various method signatures
- ValidatorTest: catching missing @param, @return, @throws
- CoverageTest: percentage calculation, edge cases (0 methods, all documented)
- EdgeCaseTest: methods with no params, void return, multiple exceptions

### 5. CI/CD exit codes
- validate exits with code 1 if any ERROR severity issues are found
- generate and coverage always exit 0 on success

### 6. README.md
- Match structure of PyCodeCommenter README
- Sections: what it is, why it exists, installation, CLI usage,
  library usage, features, roadmap

## Code Rules
- Every public class and method must have Javadoc
- Use picocli @Command annotations for CLI, not manual args[] parsing
- Do not reorganise the package structure
- No Java preview features
- Follow naming conventions already in the codebase
- String formatting for console output uses System.out.printf where applicable

## What NOT to Do
- Do not add dependencies beyond what is in pom.xml without asking
- Do not rename existing classes or packages
- Do not flatten the package structure
- Do not use external HTTP calls or AI APIs — this tool is fully deterministic
- Do not modify pom.xml groupId, artifactId, or Java version
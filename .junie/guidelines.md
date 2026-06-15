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
- Java version: 17 (LTS)
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

## Java Implementation Playbook
**Goal:** Build JavaCodeCommenter not as a Python clone, but as a robust, idiomatic
Java application that leverages the language's core strengths: strong typing,
object-oriented design, robust standard library, modern features, and inherent
performance capabilities.

### Phase 1: Strategic Object-Oriented Design (OOP First)
Think about nouns and their behaviors, not scripts or functions.

**Core Entities and Responsibilities:**
- `CodeFile` — encapsulates Path, methods like readLines(), writeLines(), getLanguage()
- `CodeLine` — represents a single line with getText(), isComment(), isWhitespace()
- `Comment` interface — defines getType() (SINGLE_LINE, MULTI_LINE) and getContent(),
  with concrete implementations SingleLineComment and MultiLineComment
- Use Records (Java 16+) for immutable data carriers:
  `record CommentConfig(String prefix, String suffix, boolean enabled)`

**Encapsulation rules:**
- All internal state private
- Expose state via well-defined public methods
- Collections exposed via streaming API or unmodifiable views, never raw references

**Abstraction via Interfaces:**
- `CommentStrategy` interface with `applyComment(List<String> lines, int lineNumber,
  String commentText)` — allows swapping BlockCommentStrategy vs LineCommentStrategy
- `AstAnalyzer` interface with `analyze(CodeFile file)` for future deep parsing

**Polymorphism:**
- Use `List<Comment>` where elements can be SingleLineComment or MultiLineComment,
  processed uniformly through the Comment interface

### Phase 2: Strong Typing and Standard Library

**Type discipline:**
- `List<String> lines` not raw `List`
- `Path filePath` not `String filePath` for file operations
- Explicit types on all variables, parameters, and return values

**Collections — choose the right structure:**
- `ArrayList<String>` for ordered lines (fast random access)
- `HashSet<String>` for unique keywords or file extensions to ignore
- `HashMap<String, CommentStyle>` for mapping extensions to comment styles

**File I/O — use NIO.2 exclusively:**
```java
// Reading
Files.readAllLines(filePath, StandardCharsets.UTF_8);

// Writing
Files.write(outputFilePath, modifiedLines, StandardCharsets.UTF_8);
```
Never use legacy `java.io.File` for new code.

**String handling:**
- `StringBuilder` for all string concatenation in loops, never `+` in loops
- `Pattern` and `Matcher` for regex-based comment detection:
  `Pattern.compile("^\\s*//.*$")` for single-line comments

**Null safety utilities:**
- `Objects.requireNonNull()` for early null checks in constructors and public methods
- `Paths.get()` for platform-agnostic path construction

### Phase 3: Modern Java Features (Java 8+)

**Streams API:**
```java
List<String> modifiedLines = Files.lines(filePath)
    .map(line -> commentProcessor.processLine(line))
    .collect(Collectors.toList());
```
Use streams for all collection transformations — filter, map, reduce, collect.

**Lambdas:**
Use for single-method interface implementations, Comparators, and callbacks.
`lines.sort((l1, l2) -> l1.length() - l2.length());`

**Optional<T>:**
Return Optional instead of null when a value may be absent:
`Optional<Integer> findMethodDeclaration(List<String> lines)`
Never return null from a public method.

**Records (Java 16+):**
For immutable data carriers — auto-generates constructor, getters, equals,
hashCode, toString:
`record CommentPosition(int lineNumber, String commentText)`

**Text Blocks (Java 15+):**
For multi-line string templates:
```java
String template = """
    /**
     * %s
     */
    """;
```

### Phase 4: Robustness and Maintainability

**Exception handling:**
- Use specific types: `FileNotFoundException`, `IOException`, `IllegalArgumentException`
- Checked exceptions for recoverable errors (file not found, bad input)
- RuntimeExceptions for programming errors
- Create custom exceptions where meaningful: `CommenterConfigurationException`
- Always provide meaningful messages, never swallow exceptions silently

**Logging:**
Do not use System.out.println for internal diagnostics. Use java.util.logging
or add SLF4J + Logback as a dependency when logging is needed:
```java
private static final Logger logger = LoggerFactory.getLogger(JavaCodeCommenter.class);
logger.info("Processing file: {}", filePath);
```
System.out.printf is acceptable for intentional CLI output only.

**Javadoc:**
Every public class, method, constructor, and field must have Javadoc.
This tool validates its own source — it must pass its own coverage check at 100%.

### Phase 5: Performance (Future Considerations)

**Efficient string handling:**
Always StringBuilder for concatenation in loops.

**Concurrency for directory scanning:**
When processing large projects, consider ExecutorService for parallel file processing:
```java
ExecutorService executor = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
);
// Submit each file as a Callable<CoverageReport>
```
Not required for v1.x but keep the design open for it — avoid shared mutable state
in analyzers so they can be made concurrent later without refactoring.

## Improvement Roadmap

### v1.1.0 — Accuracy
- Better summary generation: `calculateDiscount` → "Calculates a discount based
  on price and rate." not "Calculate discount."
- Smarter @param descriptions: infer meaning from parameter name, not just echo it
- Handle constructors, not just methods
- Handle class-level Javadoc

### v1.2.0 — Developer Experience
- Coloured terminal output (green checkmarks, red errors) like PyCodeCommenter
- --threshold flag for coverage (exit 1 if below threshold, e.g. --threshold 80)
- Support passing multiple file arguments in one command

### v1.3.0 — Ecosystem
- Maven plugin: mvn javacodecommenter:validate
- IntelliJ plugin: right-click a method → "Generate Javadoc"
- VS Code extension

### v2.0.0 — Intelligence
- Optional AI-powered generation (Gemini API) for genuinely useful descriptions
- Smart update: preserve human-written content when regenerating
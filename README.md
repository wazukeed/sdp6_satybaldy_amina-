# Smart Code Inspection & Compliance Engine

This project is a small Java application for checking a software project. It uses the Visitor design pattern. Five visitors inspect one project model and create different reports.

## Technologies

- Java 17
- Maven
- JUnit 5
- Mermaid for UML diagram source

## Project structure

```text
src/main/java/org/example/       Java elements, visitors, and Main
src/test/java/org/example/       JUnit tests
docs/class-diagram.mmd           Class diagram source
docs/double-dispatch-sequence.mmd Sequence diagram source
pom.xml                          Maven build and test setup
```

The demo represents this project tree in Java objects:

```text
fintech-project/
├── src/
│   ├── PaymentService.java
│   └── UserService.java
├── tests/
│   └── PaymentServiceTest.java
├── config/
│   └── application.properties
└── pom.xml
```

The `pom.xml` project element contains two example dependencies. The demo has no known vulnerable or prohibited dependencies. It has one missing production test, so the compliance report points this out.

## Visitor Pattern

The elements are `SourceCodeFile`, `TestFile`, `ConfigurationFile`, `DependencyFile`, and `Directory`. Each element implements `ProjectElement`. `Directory` can contain files and other directories.

Each visitor has a different operation:

- `SecurityAuditVisitor` looks for credentials, vulnerable dependencies, insecure settings, and high-risk source files. It also calculates a score.
- `ProjectStatisticsVisitor` counts files, lines, source and test files, and dependencies. It also calculates complexity and test/source ratio.
- `QualityGateVisitor` checks the complexity, test ratio, and critical security thresholds.
- `ComplianceVisitor` checks exposed credentials, dependency versions, production tests, and prohibited dependencies.
- `TechnicalDebtVisitor` adds debt points for complexity and risks.

### Double dispatch

The caller first invokes `element.accept(visitor)`. Each element's `accept` method then calls the matching overloaded method, such as `visitor.visit(this)`. The real element type chooses the correct visitor method. This two-step choice is called double dispatch. The implementation does not need `instanceof` or a type switch.

## Questions and answers

1. **What problem does Visitor solve?** It adds new operations to a group of stable object types without putting every operation inside those object classes.
2. **Where are Element and Visitor?** `ProjectElement` is the Element interface. `ProjectVisitor` is the Visitor interface. The five file and directory classes are concrete Elements.
3. **What is double dispatch?** The element receives `accept`, then calls the visitor's matching `visit` overload. The element type and visitor type both select behavior.
4. **Why not put all operations inside Element classes?** The classes would grow to contain statistics, security, compliance, and debt logic. Visitor keeps these separate and easier to add or change.
5. **Is adding a new Visitor or Element easier?** A new Visitor is easier: implement the five overloads. A new Element requires updating `ProjectVisitor` and each visitor, as well as the new element itself.
6. **When is Visitor a bad choice?** It is a poor fit when element types change often, when there are only a few simple operations, or when visitor methods need to change element data.
7. **How is Visitor different from Strategy?** Strategy selects one interchangeable algorithm for a task. Visitor runs operations across different element types and selects an overload for each type.
8. **Which SOLID principles are used?** Single Responsibility is supported because reports live in separate visitor classes. Open/Closed is supported for adding visitors without changing the existing elements. Dependency Inversion is shown by elements accepting the `ProjectVisitor` interface.

### Adding `DatabaseMigrationFile`

Create `DatabaseMigrationFile` as a `ProjectElement`, give it migration data and an `accept` method that calls `visitor.visit(this)`. Add `visit(DatabaseMigrationFile)` to `ProjectVisitor`, then implement that method in all five visitors. Update the class diagram and add tests for the new element and visitor behavior. This is the main cost of Visitor: adding an element changes every visitor.

## Run the application

From the project directory:

```bash
mvn clean package
java -cp target/classes org.example.Main
```

## Run JUnit tests

```bash
mvn test
```

Tests check typed dispatch, element data, nested and empty directories, security findings, statistics, passing and failing quality gates, compliance violations, and technical debt.

## Example output

```text
=== Smart Code Inspection & Compliance Engine ===
Project Statistics
Total files: 5
Total lines of code: 26
Source files: 2
Test files: 1
Dependencies: 2
Average complexity: 7.00
Test/source ratio: 0.50

Security Audit
Security score: 100/100
Critical issues: 0
No security issues found.

Quality Gate
PASS - Average complexity <= 10 (7.00)
PASS - Test/source ratio >= 0.40 (0.50)
PASS - Critical security issues = 0 (0)
Release decision: PASS - APPROVED

Compliance Report
Status: NON-COMPLIANT
- UserService has no matching production test

Technical Debt Report
Complexity debt: 8 points
Security debt: 0 points
Configuration debt: 0 points
Dependency debt: 0 points
Total technical debt: 8 points
```

The statistics report also prints total lines of code. The compliance report intentionally shows that `UserService` needs a test. The two source files add 8 technical debt points because their complexity values are above the debt baseline of 5.

## UML diagrams

- [Class diagram source](docs/class-diagram.mmd)
- [Double-dispatch sequence source](docs/double-dispatch-sequence.mmd)

The Mermaid files can be opened in an editor or renderer that supports Mermaid.

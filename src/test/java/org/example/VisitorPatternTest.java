package org.example;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitorPatternTest {
    @Test
    void sourceCodeFileKeepsItsDataAndRejectsNegativeComplexity() {
        SourceCodeFile file = new SourceCodeFile("src/Pay.java", "class Pay {}", 4, true);

        assertEquals("src/Pay.java", file.getPath());
        assertEquals("class Pay {}", file.getContent());
        assertEquals(4, file.getComplexity());
        assertTrue(file.isHighRisk());
        assertThrows(IllegalArgumentException.class, () -> new SourceCodeFile("x", "", -1, false));
    }

    @Test
    void testFileStoresCoveredClasses() {
        TestFile file = new TestFile("tests/PayTest.java", "test", List.of("Pay"));

        assertEquals(List.of("Pay"), file.getTestedClasses());
        assertThrows(UnsupportedOperationException.class, () -> file.getTestedClasses().add("Other"));
    }

    @Test
    void configurationFileStoresItsContent() {
        ConfigurationFile file = new ConfigurationFile("app.properties", "debug=false");

        assertEquals("app.properties", file.getPath());
        assertEquals("debug=false", file.getContent());
    }

    @Test
    void dependencyFileStoresAnImmutableDependencyList() {
        Dependency dependency = new Dependency("sample:library", "1.2.0", false, false);
        DependencyFile file = new DependencyFile("pom.xml", List.of(dependency));

        assertEquals(List.of(dependency), file.getDependencies());
        assertThrows(UnsupportedOperationException.class, () -> file.getDependencies().clear());
    }

    @Test
    void dependencyNormalizesAMissingVersion() {
        Dependency dependency = new Dependency("sample:library", null, false, false);

        assertEquals("", dependency.version());
    }

    @Test
    void everyElementUsesItsTypedVisitorMethod() {
        RecordingVisitor visitor = new RecordingVisitor();

        new SourceCodeFile("A.java", "", 0, false).accept(visitor);
        assertEquals("source", visitor.lastVisited);
        new TestFile("ATest.java", "", List.of()).accept(visitor);
        assertEquals("test", visitor.lastVisited);
        new ConfigurationFile("app.properties", "").accept(visitor);
        assertEquals("configuration", visitor.lastVisited);
        new DependencyFile("pom.xml", List.of()).accept(visitor);
        assertEquals("dependency", visitor.lastVisited);
        new Directory("empty").accept(visitor);
        assertEquals("directory", visitor.lastVisited);
    }

    @Test
    void directoryCanContainNestedDirectories() {
        Directory root = new Directory("root");
        Directory nested = new Directory("nested");
        nested.add(new SourceCodeFile("nested/A.java", "class A {}", 3, false));
        root.add(nested);

        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        root.accept(statistics);

        assertEquals(1, statistics.getTotalFiles());
        assertEquals(1, statistics.getSourceFiles());
    }

    @Test
    void emptyDirectoryHasZeroStatistics() {
        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        new Directory("empty").accept(statistics);

        assertEquals(0, statistics.getTotalFiles());
        assertEquals(0, statistics.getTotalLinesOfCode());
        assertEquals(0.0, statistics.getAverageComplexity());
        assertEquals(0.0, statistics.getTestSourceRatio());
    }

    @Test
    void securityAuditFindsAnExposedPassword() {
        Directory root = new Directory("root");
        root.add(new ConfigurationFile("app.properties", "password=not-a-placeholder"));
        SecurityAuditVisitor audit = audit(root);

        assertEquals(1, audit.getCriticalIssueCount());
        assertTrue(audit.getReport().contains("hardcoded credential"));
    }

    @Test
    void securityAuditFindsVulnerableDependencies() {
        Directory root = new Directory("root");
        root.add(new DependencyFile("pom.xml",
                List.of(new Dependency("old:library", "1.0", true, false))));
        SecurityAuditVisitor audit = audit(root);

        assertEquals(1, audit.getCriticalIssueCount());
        assertTrue(audit.getReport().contains("old:library"));
    }

    @Test
    void securityAuditFindsInsecureConfigurationAndHighRiskSource() {
        Directory root = new Directory("root");
        root.add(new ConfigurationFile("app.properties", "server.ssl.enabled=false"));
        root.add(new SourceCodeFile("src/Auth.java", "class Auth {}", 3, true));
        SecurityAuditVisitor audit = audit(root);

        assertEquals(2, audit.getFindings().size());
        assertEquals(0, audit.getCriticalIssueCount());
        assertEquals(70, audit.getSecurityScore());
    }

    @Test
    void securityScoreNeverFallsBelowZero() {
        Directory root = new Directory("root");
        root.add(new ConfigurationFile("one", "password=first"));
        root.add(new ConfigurationFile("two", "password=second"));
        root.add(new ConfigurationFile("three", "password=third"));
        root.add(new ConfigurationFile("four", "password=fourth"));

        assertEquals(0, audit(root).getSecurityScore());
    }

    @Test
    void securityAuditDoesNotFlagEnvironmentPlaceholders() {
        Directory root = new Directory("root");
        root.add(new ConfigurationFile("app.properties", "password=${PAYMENT_PASSWORD}"));

        assertTrue(audit(root).getFindings().isEmpty());
    }

    @Test
    void statisticsCountFilesLinesDependenciesAndRatios() {
        Directory root = new Directory("root");
        root.add(new SourceCodeFile("A.java", "line one\nline two", 6, false));
        root.add(new SourceCodeFile("B.java", "line one", 10, false));
        root.add(new TestFile("ATest.java", "test line", List.of("A")));
        root.add(new ConfigurationFile("app.properties", "x=1"));
        root.add(new DependencyFile("pom.xml", List.of(
                new Dependency("a", "1", false, false), new Dependency("b", "2", false, false))));

        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        root.accept(statistics);

        assertEquals(5, statistics.getTotalFiles());
        assertEquals(5, statistics.getTotalLinesOfCode());
        assertEquals(2, statistics.getSourceFiles());
        assertEquals(1, statistics.getTestFiles());
        assertEquals(2, statistics.getDependencies());
        assertEquals(8.0, statistics.getAverageComplexity());
        assertEquals(0.5, statistics.getTestSourceRatio());
    }

    @Test
    void qualityGatePassesWhenAllThresholdsAreMet() {
        Directory root = new Directory("root");
        root.add(new SourceCodeFile("A.java", "source", 8, false));
        root.add(new TestFile("ATest.java", "test", List.of("A")));
        ProjectStatisticsVisitor statistics = statistics(root);
        SecurityAuditVisitor security = audit(root);

        QualityGateVisitor gate = new QualityGateVisitor(statistics, security);
        root.accept(gate);

        assertTrue(gate.getReport().contains("PASS - APPROVED"));
        assertEquals(3, gate.getCriteria().size());
        assertTrue(gate.isReleaseAllowed());
    }

    @Test
    void qualityGateFailsForHighComplexity() {
        Directory root = new Directory("root");
        root.add(new SourceCodeFile("A.java", "", 11, false));
        root.add(new TestFile("ATest.java", "", List.of("A")));
        QualityGateVisitor gate = qualityGateFor(root);

        assertTrue(gate.getReport().contains("FAIL - Average complexity <= 10"));
        assertFalse(gate.isReleaseAllowed());
    }

    @Test
    void qualityGateFailsForLowTestRatio() {
        Directory root = new Directory("root");
        root.add(new SourceCodeFile("A.java", "", 2, false));
        root.add(new SourceCodeFile("B.java", "", 2, false));
        QualityGateVisitor gate = qualityGateFor(root);

        assertTrue(gate.getReport().contains("FAIL - Test/source ratio >= 0.40"));
    }

    @Test
    void qualityGateFailsForCriticalSecurityIssues() {
        Directory root = new Directory("root");
        root.add(new SourceCodeFile("A.java", "secret=exposed", 2, false));
        root.add(new TestFile("ATest.java", "", List.of("A")));
        QualityGateVisitor gate = qualityGateFor(root);

        assertTrue(gate.getReport().contains("FAIL - Critical security issues = 0"));
    }

    @Test
    void complianceFindsCredentialsMissingVersionsProhibitedDependenciesAndMissingTests() {
        Directory root = new Directory("root");
        root.add(new SourceCodeFile("src/A.java", "class A { String token = \"exposed\"; }", 1, false));
        root.add(new DependencyFile("pom.xml", List.of(
                new Dependency("sample:no-version", "", false, false),
                new Dependency("sample:blocked", "1.0", false, true))));
        ComplianceVisitor compliance = compliance(root);

        assertFalse(compliance.isCompliant());
        String report = compliance.getReport();
        assertTrue(report.contains("exposed credential"));
        assertTrue(report.contains("no specified version"));
        assertTrue(report.contains("is prohibited"));
        assertTrue(report.contains("A has no matching production test"));
    }

    @Test
    void compliancePassesWhenEveryProductionClassHasTestsAndDependenciesAreAllowed() {
        Directory root = new Directory("root");
        root.add(new SourceCodeFile("src/A.java", "class A {}", 1, false));
        root.add(new TestFile("tests/ATest.java", "test", List.of("A")));
        root.add(new DependencyFile("pom.xml",
                List.of(new Dependency("sample:library", "1.0", false, false))));
        ComplianceVisitor compliance = compliance(root);
        compliance.getReport();

        assertTrue(compliance.isCompliant());
        assertTrue(compliance.getViolations().isEmpty());
    }

    @Test
    void technicalDebtAddsComplexitySecurityConfigurationAndDependencyPoints() {
        Directory root = new Directory("root");
        root.add(new SourceCodeFile("src/A.java", "class A {}", 8, true));
        root.add(new ConfigurationFile("app.properties", "debug=true"));
        root.add(new DependencyFile("pom.xml", List.of(
                new Dependency("sample:library", "", true, true))));
        TechnicalDebtVisitor debt = new TechnicalDebtVisitor();
        root.accept(debt);

        assertEquals(6, debt.getComplexityDebt());
        assertEquals(10, debt.getSecurityDebt());
        assertEquals(15, debt.getConfigurationDebt());
        assertEquals(40, debt.getDependencyDebt());
        assertEquals(71, debt.getTotalDebt());
        assertTrue(debt.getReport().contains("Total technical debt: 71 points"));
    }

    @Test
    void technicalDebtIsZeroForAHealthyEmptyProject() {
        TechnicalDebtVisitor debt = new TechnicalDebtVisitor();
        new Directory("empty").accept(debt);

        assertEquals(0, debt.getTotalDebt());
        assertTrue(debt.getReport().contains("No technical debt was found"));
    }

    private static ProjectStatisticsVisitor statistics(ProjectElement root) {
        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        root.accept(statistics);
        return statistics;
    }

    private static SecurityAuditVisitor audit(ProjectElement root) {
        SecurityAuditVisitor audit = new SecurityAuditVisitor();
        root.accept(audit);
        return audit;
    }

    private static QualityGateVisitor qualityGateFor(ProjectElement root) {
        QualityGateVisitor gate = new QualityGateVisitor(statistics(root), audit(root));
        root.accept(gate);
        return gate;
    }

    private static ComplianceVisitor compliance(ProjectElement root) {
        ComplianceVisitor compliance = new ComplianceVisitor();
        root.accept(compliance);
        return compliance;
    }

    private static final class RecordingVisitor implements ProjectVisitor {
        private String lastVisited;

        @Override
        public void visit(SourceCodeFile file) {
            lastVisited = "source";
        }

        @Override
        public void visit(TestFile file) {
            lastVisited = "test";
        }

        @Override
        public void visit(ConfigurationFile file) {
            lastVisited = "configuration";
        }

        @Override
        public void visit(DependencyFile file) {
            lastVisited = "dependency";
        }

        @Override
        public void visit(Directory directory) {
            lastVisited = "directory";
        }
    }
}

package org.example;

import java.util.ArrayList;
import java.util.List;

public final class SecurityAuditVisitor implements ProjectVisitor {
    public record Finding(String severity, String category, String message) {
    }

    private final List<Finding> findings = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile file) {
        if (SecurityRules.hasHardcodedCredential(file.getContent())) {
            findings.add(new Finding("CRITICAL", "Credential", file.getPath() + " contains a hardcoded credential"));
        }
        if (file.isHighRisk()) {
            findings.add(new Finding("HIGH", "Source risk", file.getPath() + " is marked as a high-risk source file"));
        }
    }

    @Override
    public void visit(TestFile file) {
        if (SecurityRules.hasHardcodedCredential(file.getContent())) {
            findings.add(new Finding("CRITICAL", "Credential", file.getPath() + " contains a hardcoded credential"));
        }
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (SecurityRules.hasHardcodedCredential(file.getContent())) {
            findings.add(new Finding("CRITICAL", "Credential", file.getPath() + " contains a hardcoded credential"));
        }
        if (SecurityRules.hasInsecureSetting(file.getContent())) {
            findings.add(new Finding("HIGH", "Configuration", file.getPath() + " contains an insecure setting"));
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.getDependencies()) {
            if (dependency.vulnerable()) {
                findings.add(new Finding("CRITICAL", "Dependency",
                        dependency.name() + " " + dependency.version() + " is marked vulnerable"));
            }
        }
    }

    @Override
    public void visit(Directory directory) {
        directory.getChildren().forEach(child -> child.accept(this));
    }

    public List<Finding> getFindings() {
        return List.copyOf(findings);
    }

    public int getCriticalIssueCount() {
        return (int) findings.stream().filter(finding -> finding.severity().equals("CRITICAL")).count();
    }

    public int getSecurityScore() {
        int deductions = findings.stream()
                .mapToInt(finding -> finding.severity().equals("CRITICAL") ? 25 : 15)
                .sum();
        return Math.max(0, 100 - deductions);
    }

    public String getReport() {
        StringBuilder report = new StringBuilder("Security Audit\n");
        report.append("Security score: ").append(getSecurityScore()).append("/100\n");
        report.append("Critical issues: ").append(getCriticalIssueCount()).append('\n');
        if (findings.isEmpty()) {
            report.append("No security issues found.\n");
        } else {
            findings.forEach(finding -> report.append("- [")
                    .append(finding.severity()).append("] ")
                    .append(finding.category()).append(": ")
                    .append(finding.message()).append('\n'));
        }
        return report.toString();
    }
}

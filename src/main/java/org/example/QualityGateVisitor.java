package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class QualityGateVisitor implements ProjectVisitor {
    public record Criterion(String name, boolean passed, String detail) {
    }

    private final ProjectStatisticsVisitor statistics;
    private final SecurityAuditVisitor securityAudit;
    private final List<Criterion> criteria = new ArrayList<>();

    public QualityGateVisitor(ProjectStatisticsVisitor statistics, SecurityAuditVisitor securityAudit) {
        this.statistics = Objects.requireNonNull(statistics, "statistics");
        this.securityAudit = Objects.requireNonNull(securityAudit, "securityAudit");
    }

    @Override
    public void visit(SourceCodeFile file) {
    }

    @Override
    public void visit(TestFile file) {
    }

    @Override
    public void visit(ConfigurationFile file) {
    }

    @Override
    public void visit(DependencyFile file) {
    }

    @Override
    public void visit(Directory directory) {
        directory.getChildren().forEach(child -> child.accept(this));
    }

    public List<Criterion> getCriteria() {
        updateCriteria();
        return List.copyOf(criteria);
    }

    public boolean isReleaseAllowed() {
        updateCriteria();
        return !criteria.isEmpty() && criteria.stream().allMatch(Criterion::passed);
    }

    public String getReport() {
        updateCriteria();
        StringBuilder report = new StringBuilder("Quality Gate\n");
        criteria.forEach(criterion -> report.append(criterion.passed() ? "PASS" : "FAIL")
                .append(" - ").append(criterion.name()).append(" (")
                .append(criterion.detail()).append(")\n"));
        report.append("Release decision: ").append(isReleaseAllowed() ? "PASS - APPROVED" : "FAIL - BLOCKED")
                .append('\n');
        return report.toString();
    }

    private void updateCriteria() {
        criteria.clear();
        addCriterion("Average complexity <= 10", statistics.getAverageComplexity() <= 10,
                "%.2f".formatted(statistics.getAverageComplexity()));
        addCriterion("Test/source ratio >= 0.40", statistics.getTestSourceRatio() >= 0.40,
                "%.2f".formatted(statistics.getTestSourceRatio()));
        addCriterion("Critical security issues = 0", securityAudit.getCriticalIssueCount() == 0,
                Integer.toString(securityAudit.getCriticalIssueCount()));
    }

    private void addCriterion(String name, boolean passed, String detail) {
        criteria.add(new Criterion(name, passed, detail));
    }
}

package org.example;

import java.util.ArrayList;
import java.util.List;

public final class TechnicalDebtVisitor implements ProjectVisitor {
    private int complexityDebt;
    private int securityDebt;
    private int configurationDebt;
    private int dependencyDebt;
    private final List<String> notes = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile file) {
        int debt = Math.max(0, file.getComplexity() - 5) * 2;
        complexityDebt += debt;
        if (debt > 0) {
            notes.add(file.getPath() + ": " + debt + " complexity debt points");
        }
        if (file.isHighRisk()) {
            securityDebt += 10;
            notes.add(file.getPath() + ": 10 high-risk source debt points");
        }
        if (SecurityRules.hasHardcodedCredential(file.getContent())) {
            securityDebt += 25;
            notes.add(file.getPath() + ": 25 exposed-credential debt points");
        }
    }

    @Override
    public void visit(TestFile file) {
        if (SecurityRules.hasHardcodedCredential(file.getContent())) {
            securityDebt += 25;
            notes.add(file.getPath() + ": 25 exposed-credential debt points");
        }
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (SecurityRules.hasHardcodedCredential(file.getContent())) {
            securityDebt += 25;
            notes.add(file.getPath() + ": 25 exposed-credential debt points");
        }
        if (SecurityRules.hasInsecureSetting(file.getContent())) {
            configurationDebt += 15;
            notes.add(file.getPath() + ": 15 insecure-configuration debt points");
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.getDependencies()) {
            if (dependency.vulnerable()) {
                dependencyDebt += 20;
                notes.add(dependency.name() + ": 20 vulnerable-dependency debt points");
            }
            if (dependency.version().isBlank()) {
                dependencyDebt += 5;
                notes.add(dependency.name() + ": 5 missing-version debt points");
            }
            if (dependency.prohibited()) {
                dependencyDebt += 15;
                notes.add(dependency.name() + ": 15 prohibited-dependency debt points");
            }
        }
    }

    @Override
    public void visit(Directory directory) {
        directory.getChildren().forEach(child -> child.accept(this));
    }

    public int getComplexityDebt() {
        return complexityDebt;
    }

    public int getSecurityDebt() {
        return securityDebt;
    }

    public int getConfigurationDebt() {
        return configurationDebt;
    }

    public int getDependencyDebt() {
        return dependencyDebt;
    }

    public int getTotalDebt() {
        return complexityDebt + securityDebt + configurationDebt + dependencyDebt;
    }

    public String getReport() {
        StringBuilder report = new StringBuilder("Technical Debt Report\n");
        report.append("Complexity debt: ").append(complexityDebt).append(" points\n");
        report.append("Security debt: ").append(securityDebt).append(" points\n");
        report.append("Configuration debt: ").append(configurationDebt).append(" points\n");
        report.append("Dependency debt: ").append(dependencyDebt).append(" points\n");
        report.append("Total technical debt: ").append(getTotalDebt()).append(" points\n");
        if (notes.isEmpty()) {
            report.append("No technical debt was found.\n");
        } else {
            notes.forEach(note -> report.append("- ").append(note).append('\n'));
        }
        return report.toString();
    }
}

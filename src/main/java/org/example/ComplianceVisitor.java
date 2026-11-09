package org.example;

import java.util.ArrayList;
import java.util.List;

public final class ComplianceVisitor implements ProjectVisitor {
    private final List<String> violations = new ArrayList<>();
    private final List<SourceCodeFile> productionFiles = new ArrayList<>();
    private final List<String> testedClasses = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile file) {
        productionFiles.add(file);
        if (SecurityRules.hasHardcodedCredential(file.getContent())) {
            violations.add(file.getPath() + " contains an exposed credential");
        }
    }

    @Override
    public void visit(TestFile file) {
        testedClasses.addAll(file.getTestedClasses());
        if (SecurityRules.hasHardcodedCredential(file.getContent())) {
            violations.add(file.getPath() + " contains an exposed credential");
        }
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (SecurityRules.hasHardcodedCredential(file.getContent())) {
            violations.add(file.getPath() + " contains an exposed credential");
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.getDependencies()) {
            if (dependency.version().isBlank()) {
                violations.add(dependency.name() + " has no specified version");
            }
            if (dependency.prohibited()) {
                violations.add(dependency.name() + " is prohibited");
            }
        }
    }

    @Override
    public void visit(Directory directory) {
        directory.getChildren().forEach(child -> child.accept(this));
    }

    public List<String> getViolations() {
        checkProductionTests();
        return List.copyOf(violations);
    }

    public boolean isCompliant() {
        checkProductionTests();
        return violations.isEmpty();
    }

    public String getReport() {
        checkProductionTests();
        StringBuilder report = new StringBuilder("Compliance Report\n");
        report.append("Status: ").append(isCompliant() ? "COMPLIANT" : "NON-COMPLIANT").append('\n');
        if (violations.isEmpty()) {
            report.append("No compliance violations found.\n");
        } else {
            violations.forEach(violation -> report.append("- ").append(violation).append('\n'));
        }
        return report.toString();
    }

    private void checkProductionTests() {
        for (SourceCodeFile source : productionFiles) {
            String fileName = source.getPath().substring(source.getPath().lastIndexOf('/') + 1);
            String className = fileName.endsWith(".java")
                    ? fileName.substring(0, fileName.length() - ".java".length())
                    : fileName;
            if (!testedClasses.contains(className)) {
                String violation = className + " has no matching production test";
                if (!violations.contains(violation)) {
                    violations.add(violation);
                }
            }
        }
    }
}

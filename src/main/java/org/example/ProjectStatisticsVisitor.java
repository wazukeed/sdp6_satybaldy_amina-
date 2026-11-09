package org.example;

public final class ProjectStatisticsVisitor implements ProjectVisitor {
    private int totalFiles;
    private long totalLinesOfCode;
    private int sourceFiles;
    private int testFiles;
    private int dependencies;
    private int totalComplexity;

    @Override
    public void visit(SourceCodeFile file) {
        totalFiles++;
        sourceFiles++;
        totalLinesOfCode += file.getContent().lines().count();
        totalComplexity += file.getComplexity();
    }

    @Override
    public void visit(TestFile file) {
        totalFiles++;
        testFiles++;
        totalLinesOfCode += file.getContent().lines().count();
    }

    @Override
    public void visit(ConfigurationFile file) {
        totalFiles++;
        totalLinesOfCode += file.getContent().lines().count();
    }

    @Override
    public void visit(DependencyFile file) {
        totalFiles++;
        dependencies += file.getDependencies().size();
    }

    @Override
    public void visit(Directory directory) {
        directory.getChildren().forEach(child -> child.accept(this));
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public long getTotalLinesOfCode() {
        return totalLinesOfCode;
    }

    public int getSourceFiles() {
        return sourceFiles;
    }

    public int getTestFiles() {
        return testFiles;
    }

    public int getDependencies() {
        return dependencies;
    }

    public double getAverageComplexity() {
        return sourceFiles == 0 ? 0.0 : (double) totalComplexity / sourceFiles;
    }

    public double getTestSourceRatio() {
        return sourceFiles == 0 ? 0.0 : (double) testFiles / sourceFiles;
    }

    public String getReport() {
        return """
                Project Statistics
                Total files: %d
                Total lines of code: %d
                Source files: %d
                Test files: %d
                Dependencies: %d
                Average complexity: %.2f
                Test/source ratio: %.2f
                """.formatted(totalFiles, totalLinesOfCode, sourceFiles, testFiles, dependencies,
                getAverageComplexity(), getTestSourceRatio());
    }
}

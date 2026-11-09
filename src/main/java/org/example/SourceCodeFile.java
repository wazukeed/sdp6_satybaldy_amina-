package org.example;

import java.util.Objects;

public final class SourceCodeFile implements ProjectElement {
    private final String path;
    private final String content;
    private final int complexity;
    private final boolean highRisk;

    public SourceCodeFile(String path, String content, int complexity, boolean highRisk) {
        this.path = Objects.requireNonNull(path, "path");
        this.content = Objects.requireNonNull(content, "content");
        if (complexity < 0) {
            throw new IllegalArgumentException("Complexity cannot be negative");
        }
        this.complexity = complexity;
        this.highRisk = highRisk;
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    public int getComplexity() {
        return complexity;
    }

    public boolean isHighRisk() {
        return highRisk;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}

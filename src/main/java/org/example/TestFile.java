package org.example;

import java.util.List;
import java.util.Objects;

public final class TestFile implements ProjectElement {
    private final String path;
    private final String content;
    private final List<String> testedClasses;

    public TestFile(String path, String content, List<String> testedClasses) {
        this.path = Objects.requireNonNull(path, "path");
        this.content = Objects.requireNonNull(content, "content");
        this.testedClasses = List.copyOf(Objects.requireNonNull(testedClasses, "testedClasses"));
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    public List<String> getTestedClasses() {
        return testedClasses;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}

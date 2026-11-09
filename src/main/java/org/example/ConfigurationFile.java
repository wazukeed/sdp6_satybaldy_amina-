package org.example;

import java.util.Objects;

public final class ConfigurationFile implements ProjectElement {
    private final String path;
    private final String content;

    public ConfigurationFile(String path, String content) {
        this.path = Objects.requireNonNull(path, "path");
        this.content = Objects.requireNonNull(content, "content");
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}

package org.example;

import java.util.List;
import java.util.Objects;

public final class DependencyFile implements ProjectElement {
    private final String path;
    private final List<Dependency> dependencies;

    public DependencyFile(String path, List<Dependency> dependencies) {
        this.path = Objects.requireNonNull(path, "path");
        this.dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
    }

    public String getPath() {
        return path;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}

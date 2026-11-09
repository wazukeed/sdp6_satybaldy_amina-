package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Directory implements ProjectElement {
    private final String name;
    private final List<ProjectElement> children = new ArrayList<>();

    public Directory(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getName() {
        return name;
    }

    public void add(ProjectElement element) {
        children.add(Objects.requireNonNull(element, "element"));
    }

    public List<ProjectElement> getChildren() {
        return List.copyOf(children);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}

package org.example;

public interface ProjectElement {
    void accept(ProjectVisitor visitor);
}

package org.example;

import java.util.Objects;

public record Dependency(String name, String version, boolean vulnerable, boolean prohibited) {
    public Dependency {
        name = Objects.requireNonNull(name, "name");
        version = version == null ? "" : version.trim();
    }
}

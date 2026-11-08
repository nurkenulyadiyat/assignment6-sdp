package edu.assignment.visitor;

public abstract class AbstractNode implements Element {
    private final String name;
    private DirectoryNode parent;

    AbstractNode(String name) {
        if (name == null || name.isBlank() || name.equals(".") || name.equals("..")
                || name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("Name must be one non-empty path component");
        }
        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    final DirectoryNode getParent() {
        return parent;
    }

    // Package-private: DirectoryNode.add is the public ownership API.
    final void attachTo(DirectoryNode directory) {
        parent = directory;
    }
}

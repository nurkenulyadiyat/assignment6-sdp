package edu.assignment.visitor;

import java.util.Objects;

public final class TotalSizeVisitor implements Visitor {
    private long totalSize;

    public long calculate(Element root) {
        totalSize = 0;
        Objects.requireNonNull(root, "Root cannot be null").accept(this);
        return totalSize;
    }

    @Override
    public void visit(FileNode file) {
        totalSize = Math.addExact(totalSize, file.getSize());
    }

    @Override
    public void visit(DirectoryNode directory) {
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
    }
}

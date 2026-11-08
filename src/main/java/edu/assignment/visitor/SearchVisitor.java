package edu.assignment.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class SearchVisitor implements Visitor {
    private final Predicate<String> filenameCriterion;
    private final long minimumSize;
    private final List<String> matches = new ArrayList<>();
    private String currentPath = "";

    public SearchVisitor(Predicate<String> filenameCriterion, long minimumSize) {
        if (minimumSize < 0) {
            throw new IllegalArgumentException("Minimum size cannot be negative");
        }
        this.filenameCriterion = Objects.requireNonNull(filenameCriterion, "Filename criterion is required");
        this.minimumSize = minimumSize;
    }

    public List<String> search(Element root) {
        matches.clear();
        currentPath = "";
        Objects.requireNonNull(root, "Root cannot be null").accept(this);
        Collections.sort(matches);
        return List.copyOf(matches);
    }

    @Override
    public void visit(FileNode file) {
        if (filenameCriterion.test(file.getName()) && file.getSize() >= minimumSize) {
            matches.add(currentPath.isEmpty() ? file.getName() : currentPath);
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        String parentPath = currentPath;
        for (Element child : directory.getChildren()) {
            currentPath = parentPath.isEmpty() ? child.getName() : parentPath + "/" + child.getName();
            child.accept(this);
        }
        currentPath = parentPath;
    }
}

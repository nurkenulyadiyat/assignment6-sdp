package edu.assignment.visitor;

import java.util.ArrayList;
import java.util.List;

public final class DirectoryNode extends AbstractNode {
    private final List<AbstractNode> children = new ArrayList<>();

    public DirectoryNode(String name) {
        super(name);
    }

    public void add(AbstractNode child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null");
        }
        checkCycle(child);
        if (child.getParent() != null) {
            throw new IllegalArgumentException("Child already has a parent");
        }
        checkDuplicateName(child.getName());
        children.add(child);
        child.attachTo(this);
    }

    private void checkCycle(AbstractNode child) {
        for (AbstractNode ancestor = this; ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor == child) {
                throw new IllegalArgumentException("Adding this child would create a cycle");
            }
        }
    }

    private void checkDuplicateName(String name) {
        for (Element child : children) {
            if (child.getName().equals(name)) {
                throw new IllegalArgumentException("Duplicate sibling name: " + name);
            }
        }
    }

    public List<AbstractNode> getChildren() {
        return List.copyOf(children);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

package edu.assignment.visitor;

public interface Element {
    String getName();

    void accept(Visitor visitor);
}

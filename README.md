# Visitor Pattern File System

A small in-memory file system for Assignment 06. Java 17, Maven and JUnit 5.
Files and directories contain data. Visitors implement independent operations.

## Run

Install JDK 17 and Maven, then open a terminal in the folder containing `pom.xml`.

```bash
mvn test
mvn -q compile exec:java
```

Change the filename suffix and inclusive minimum size without editing code:

```bash
mvn -q compile exec:java "-Dexec.args=.txt 200"
```

## Design

`Element` declares `accept(Visitor)`. Each concrete node calls
`visitor.visit(this)`, selecting the overload for its own concrete type.
`TotalSizeVisitor` adds file sizes. `SearchVisitor` combines a configurable
`Predicate<String>` with `size >= minimumSize`, then sorts relative paths.
`StatisticsVisitor` counts files and directories and records maximum depth.

Visitors own recursive depth-first traversal. Nodes do not recurse in `accept`.
Run complete operations with `calculate(root)`, `search(root)` and `analyze(root)`; these methods
reset all accumulated state before traversal, so visitor instances are reusable.
Direct `accept` calls are the dispatch primitive, not a reset-and-run API.

Directories expose immutable snapshots of their children. `add` checks all
invariants before changing ownership: duplicate names, existing parents and
cycles are rejected with `IllegalArgumentException`. Names are case-sensitive
single path components. File sizes and search thresholds cannot be negative.

Search paths omit the selected directory's name and its ancestors, use `/`, and
are sorted by Java's natural string order. A selected file uses its own name.
The tree is single-threaded, and mutations during traversal are unsupported.
Recursive depth is limited by the JVM stack; no fixed nesting level is imposed.
Totals use `long`; overflowing the range raises `ArithmeticException`.

## Statistics extension

The third operation was added as `StatisticsVisitor` in a separate commit.
Only the new visitor, its tests, the demo client and documentation needed changes.
`FileNode`, `DirectoryNode`, their `accept` methods and both interfaces remained
unchanged. This is the extension boundary: new operations are easy, but adding
a new element type requires a new `visit` overload and updates to every visitor.

Depth starts at zero for the chosen root, increases before visiting each child,
and decreases on return. Both files and directories update the maximum, so even
an empty directory can be the deepest node. A directory root counts as one
directory; a file root counts as one file and zero directories.

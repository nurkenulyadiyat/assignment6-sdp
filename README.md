# Visitor Pattern File System

A small in-memory file system for Assignment 06. Java 17, Maven and JUnit 5.
Files and directories contain data. Visitors implement independent operations.

## Run

Install JDK 17 and Maven, then open a terminal in the folder containing `pom.xml`.
The first Maven run downloads the build plugins and JUnit, so it needs internet access.

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
Visitors are reusable sequentially, but are not thread-safe or reentrant.
Filename predicates are expected to be deterministic and not mutate the tree.

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

## Double dispatch

For `Element element = new FileNode("notes.txt", 120)`,
`element.accept(visitor)` dynamically selects `FileNode.accept`.
Inside that method, `this` has the compile-time type `FileNode`, so the compiler
selects `visit(FileNode)`. The call then dynamically selects the implementation
on the concrete visitor, such as `TotalSizeVisitor.visit(FileNode)`.
Overload selection itself is compile-time; the two object-dependent calls
produce the double-dispatch behavior. No type-checking chain is used.

## Demo data and output

`App.createDemoTree()` creates the root directory `workspace` and the following
files. In addition, it includes the empty directory `empty`.

| Root-relative path | Size in bytes | Depth |
| --- | ---: | ---: |
| `notes.txt` | 120 | 1 |
| `images/logo.png` | 500 | 2 |
| `docs/manual.txt` | 300 | 2 |
| `docs/todo.txt` | 80 | 2 |
| `docs/archive/old.txt` | 200 | 3 |
| `data.bin` | 1024 | 1 |

Default output from `mvn -q compile exec:java`:

```text
Visitor Pattern - In-memory File System
Total size: 2224 bytes
Search (name ends with ".txt", size >= 100):
  docs/archive/old.txt
  docs/manual.txt
  notes.txt
Files: 6
Directories (including root): 5
Maximum depth (root = 0): 3
```

For `-Dexec.args=.txt 200`, only `docs/archive/old.txt` and `docs/manual.txt`
match. `old.txt` demonstrates the inclusive boundary. The totals and statistics
stay unchanged because the search filter does not filter other operations.

For a different filename rule, create a visitor with another predicate:

```java
SearchVisitor search = new SearchVisitor(name -> name.startsWith("manual"), 100);
List<String> result = search.search(root);
```

## Automated verification

Verified with OpenJDK 17.0.15, Maven 3.9.9 and JUnit Jupiter 5.10.2.
Run from the project root:

```bash
mvn test
```

| Test class | Tests | Main checks |
| --- | ---: | --- |
| `VisitorTest` | 11 | Dispatch, nested totals, combined filters, inclusive size, sorted relative paths, empty/no-match cases, reuse, file roots, overflow |
| `TreeIntegrityTest` | 9 | Invalid sizes/names, duplicates, multiple parents, cycles, rejected mutation atomicity, immutable children, valid nesting |
| `StatisticsVisitorTest` | 5 | Counts, depth, empty root and deep empty directories, file root, repeated runs and operation order |

Actual result:

```text
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The tests use independently constructed fixtures. The size/search fixture totals
860 bytes; the statistics fixture contains 3 files, 5 directories and depth 3.

## Requirement coverage

| Requirement | Implementation or evidence |
| --- | --- |
| R1 Object model | `Element`, `AbstractNode`, `FileNode`, `DirectoryNode` |
| R2 Double dispatch | Both concrete `accept` methods call `visitor.visit(this)` |
| R3 Size | `TotalSizeVisitor` |
| R4 Search | Configurable `Predicate<String>`, inclusive size, sorted relative paths |
| R5 Statistics | `StatisticsVisitor`, including root and empty directories |
| R6 Tree integrity | `DirectoryNode.add` and constructors |
| R7 Traversal | Visitors own depth-first traversal; `accept` does not recurse |
| R8 Extension | Separate statistics commit; element classes and interfaces unchanged |
| T1 | `VisitorTest.fileAcceptCallsOnlyTheFileOverload`, `directoryAcceptCallsOnlyTheDirectoryOverload` |
| T2 | `VisitorTest.totalSizeIncludesFilesAtEveryNestingLevelExactlyOnce` |
| T3 | `VisitorTest.searchCombinesTheNameCriterionAndInclusiveMinimumSize` |
| T4 | `VisitorTest.searchPathsAreSortedAndRelativeToTheSelectedRoot` |
| T5 | `StatisticsVisitorTest.nestedStatisticsIncludeEveryFileAndDirectory`, `aDeepEmptyDirectoryDeterminesMaximumDepth` |
| T6 | Empty and no-match tests in `VisitorTest` and `StatisticsVisitorTest` |
| T7 | Repeat-run tests in `VisitorTest` and `StatisticsVisitorTest` |
| T8 | `TreeIntegrityTest` |

## Extension and traversal trade-offs

The three visitors each contain a small child loop. This keeps the project easy
to follow, but a new visitor must remember to traverse directories correctly.
A shared walker could centralize traversal if the project grew. Traversal takes
O(n) node visits for n reachable elements; search additionally sorts k matches
in O(k log k) comparisons. String building and comparison costs depend on path
length. The recursive call stack is O(h), where h is the tree height.

Adding another operation means implementing `Visitor`; existing elements remain
unchanged. Adding a new node type means extending the visitor contract and every
visitor. Private fields plus narrow getters expose only the data the operations
need. The package-private ownership hook belongs to the trusted model package;
the public mutation API is `DirectoryNode.add`.

## Documentation

- [Project report](docs/Visitor_Pattern_Report.pdf)
- [UML diagram](docs/uml.png)

package edu.assignment.visitor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsVisitorTest {
    @Test
    void nestedStatisticsIncludeEveryFileAndDirectory() {
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.analyze(sampleTree());

        assertStatistics(visitor, 3, 5, 3);
    }

    @Test
    void anEmptyDirectoryCountsAsTheRootAtDepthZero() {
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.analyze(new DirectoryNode("empty"));

        assertStatistics(visitor, 0, 1, 0);
    }

    @Test
    void aDeepEmptyDirectoryDeterminesMaximumDepth() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        DirectoryNode third = new DirectoryNode("third");
        root.add(new FileNode("shallow.txt", 1));
        root.add(first);
        first.add(second);
        second.add(third);
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.analyze(root);

        assertStatistics(visitor, 1, 4, 3);
    }

    @Test
    void aFileRootHasNoDirectoriesAndDepthZero() {
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.analyze(new FileNode("only.txt", 50));

        assertStatistics(visitor, 1, 0, 0);
    }

    @Test
    void repeatedRunsResetEveryCounterAndIgnoreOtherOperationOrder() {
        DirectoryNode root = sampleTree();
        StatisticsVisitor visitor = new StatisticsVisitor();
        visitor.analyze(root);
        assertStatistics(visitor, 3, 5, 3);

        new SearchVisitor(name -> true, 0).search(root);
        new TotalSizeVisitor().calculate(root);
        visitor.analyze(root);
        assertStatistics(visitor, 3, 5, 3);

        visitor.analyze(new FileNode("new.txt", 1));
        assertStatistics(visitor, 1, 0, 0);
        visitor.analyze(new DirectoryNode("empty"));
        assertStatistics(visitor, 0, 1, 0);
    }

    private void assertStatistics(StatisticsVisitor visitor, int files, int directories, int depth) {
        assertEquals(files, visitor.getFileCount());
        assertEquals(directories, visitor.getDirectoryCount());
        assertEquals(depth, visitor.getMaxDepth());
    }

    private DirectoryNode sampleTree() {
        DirectoryNode root = new DirectoryNode("reports");
        DirectoryNode notes = new DirectoryNode("notes");
        DirectoryNode deep = new DirectoryNode("deep");
        DirectoryNode empty = new DirectoryNode("empty");
        root.add(new FileNode("a.log", 5));
        notes.add(new FileNode("b.md", 10));
        deep.add(new FileNode("c.txt", 15));
        notes.add(deep);
        empty.add(new DirectoryNode("leaf"));
        root.add(notes);
        root.add(empty);
        return root;
    }
}

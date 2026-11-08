package edu.assignment.visitor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisitorTest {
    @Test
    void fileAcceptCallsOnlyTheFileOverload() {
        FileNode file = new FileNode("notes.txt", 90);
        RecordingVisitor visitor = new RecordingVisitor();

        file.accept(visitor);

        assertSame(file, visitor.file);
        assertEquals(1, visitor.fileVisits);
        assertEquals(0, visitor.directoryVisits);
    }

    @Test
    void directoryAcceptCallsOnlyTheDirectoryOverload() {
        DirectoryNode directory = new DirectoryNode("docs");
        directory.add(new FileNode("child.txt", 20));
        RecordingVisitor visitor = new RecordingVisitor();

        directory.accept(visitor);

        assertSame(directory, visitor.directory);
        assertEquals(1, visitor.directoryVisits);
        assertEquals(0, visitor.fileVisits);
    }

    @Test
    void totalSizeIncludesFilesAtEveryNestingLevelExactlyOnce() {
        assertEquals(860, new TotalSizeVisitor().calculate(sampleTree()));
    }

    @Test
    void searchCombinesTheNameCriterionAndInclusiveMinimumSize() {
        DirectoryNode root = sampleTree();
        SearchVisitor textFiles = new SearchVisitor(name -> name.endsWith(".txt"), 100);
        SearchVisitor guideFiles = new SearchVisitor(name -> name.startsWith("g"), 200);

        assertEquals(List.of("docs/guide.txt", "docs/plan.txt"), textFiles.search(root));
        assertEquals(List.of("docs/guide.txt"), guideFiles.search(root));
    }

    @Test
    void searchPathsAreSortedAndRelativeToTheSelectedRoot() {
        DirectoryNode outside = new DirectoryNode("outside");
        DirectoryNode selected = new DirectoryNode("selected");
        DirectoryNode inner = new DirectoryNode("inner");
        selected.add(new FileNode("z.txt", 12));
        inner.add(new FileNode("b.txt", 13));
        selected.add(inner);
        selected.add(new FileNode("a.txt", 14));
        outside.add(selected);
        SearchVisitor visitor = new SearchVisitor(name -> true, 0);

        assertEquals(List.of("selected/a.txt", "selected/inner/b.txt", "selected/z.txt"),
                visitor.search(outside));
        assertEquals(List.of("a.txt", "inner/b.txt", "z.txt"), visitor.search(selected));
    }

    @Test
    void emptyStructuresAndNoMatchSearchesReturnEmptyResults() {
        DirectoryNode empty = new DirectoryNode("empty");
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".pdf"), 0);

        assertEquals(0, new TotalSizeVisitor().calculate(empty));
        assertEquals(List.of(), visitor.search(empty));
        assertEquals(List.of(), visitor.search(sampleTree()));
    }

    @Test
    void repeatedSizeCalculationsResetTheTotal() {
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        DirectoryNode root = sampleTree();

        assertEquals(860, visitor.calculate(root));
        assertEquals(860, visitor.calculate(root));
        assertEquals(0, visitor.calculate(new DirectoryNode("empty")));
        assertEquals(7, visitor.calculate(new FileNode("new.txt", 7)));
    }

    @Test
    void repeatedSearchesResetStateAndKeepEarlierResultsUnchanged() {
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 100);
        DirectoryNode root = sampleTree();
        List<String> firstResult = visitor.search(root);
        DirectoryNode otherRoot = new DirectoryNode("other");
        otherRoot.add(new FileNode("fresh.txt", 150));

        assertEquals(firstResult, visitor.search(root));
        assertEquals(List.of("fresh.txt"), visitor.search(otherRoot));
        assertEquals(List.of("docs/guide.txt", "docs/plan.txt"), firstResult);
        assertThrows(UnsupportedOperationException.class, () -> firstResult.add("fake.txt"));
    }

    @Test
    void aFileRootUsesItsOwnNameAsTheSearchPath() {
        FileNode file = new FileNode("only.txt", 17);
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 17);

        assertEquals(17, new TotalSizeVisitor().calculate(file));
        assertEquals(List.of("only.txt"), visitor.search(file));
    }

    @Test
    void invalidSearchConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(name -> true, -1));
        assertThrows(NullPointerException.class, () -> new SearchVisitor(null, 0));
    }

    @Test
    void totalSizeUsesLongValuesAndRejectsOverflow() {
        DirectoryNode large = new DirectoryNode("large");
        large.add(new FileNode("first.bin", 3_000_000_000L));
        large.add(new FileNode("second.bin", 2_000_000_000L));
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        assertEquals(5_000_000_000L, visitor.calculate(large));

        DirectoryNode overflow = new DirectoryNode("overflow");
        overflow.add(new FileNode("maximum.bin", Long.MAX_VALUE));
        overflow.add(new FileNode("one.bin", 1));
        assertThrows(ArithmeticException.class, () -> visitor.calculate(overflow));
        assertEquals(5_000_000_000L, visitor.calculate(large));
    }

    private DirectoryNode sampleTree() {
        DirectoryNode root = new DirectoryNode("study");
        DirectoryNode docs = new DirectoryNode("docs");
        DirectoryNode drafts = new DirectoryNode("drafts");
        DirectoryNode media = new DirectoryNode("media");
        root.add(new FileNode("notes.txt", 90));
        docs.add(new FileNode("plan.txt", 100));
        docs.add(new FileNode("guide.txt", 250));
        drafts.add(new FileNode("draft.txt", 20));
        docs.add(drafts);
        media.add(new FileNode("icon.svg", 400));
        root.add(media);
        root.add(docs);
        root.add(new DirectoryNode("empty"));
        return root;
    }

    private static class RecordingVisitor implements Visitor {
        private FileNode file;
        private DirectoryNode directory;
        private int fileVisits;
        private int directoryVisits;

        @Override
        public void visit(FileNode file) {
            this.file = file;
            fileVisits++;
        }

        @Override
        public void visit(DirectoryNode directory) {
            this.directory = directory;
            directoryVisits++;
        }
    }
}

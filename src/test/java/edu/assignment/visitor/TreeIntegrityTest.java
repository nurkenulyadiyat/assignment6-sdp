package edu.assignment.visitor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreeIntegrityTest {
    @Test
    void negativeFileSizesAreRejectedAndZeroIsAllowed() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("bad.txt", -1));
        FileNode zero = new FileNode("zero.txt", 0);

        assertEquals(0, zero.getSize());
        assertEquals(List.of("zero.txt"), new SearchVisitor(name -> true, 0).search(zero));
    }

    @Test
    void invalidNamesCannotCreateAmbiguousPaths() {
        String[] invalidNames = {null, "", "  ", ".", "..", "a/b", "a\\b"};

        for (String name : invalidNames) {
            assertThrows(IllegalArgumentException.class, () -> new FileNode(name, 1));
            assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(name));
        }
    }

    @Test
    void duplicateSiblingNamesAreRejectedWithoutPartialChanges() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode original = new FileNode("same.txt", 10);
        FileNode duplicate = new FileNode("same.txt", 20);
        root.add(original);

        assertThrows(IllegalArgumentException.class, () -> root.add(duplicate));

        assertEquals(List.of(original), root.getChildren());
        assertSame(root, original.getParent());
        assertNull(duplicate.getParent());
    }

    @Test
    void filesAndDirectoriesAlsoShareTheSiblingNamespace() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode file = new FileNode("shared", 10);
        DirectoryNode directory = new DirectoryNode("shared");
        root.add(file);

        assertThrows(IllegalArgumentException.class, () -> root.add(directory));
        assertEquals(List.of(file), root.getChildren());
        assertNull(directory.getParent());
    }

    @Test
    void filesAndDirectoriesCannotBelongToTwoParents() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        FileNode file = new FileNode("data.bin", 10);
        DirectoryNode childDirectory = new DirectoryNode("nested");
        first.add(file);
        first.add(childDirectory);

        assertThrows(IllegalArgumentException.class, () -> second.add(file));
        assertThrows(IllegalArgumentException.class, () -> second.add(childDirectory));

        assertEquals(List.of(file, childDirectory), first.getChildren());
        assertTrue(second.getChildren().isEmpty());
        assertSame(first, file.getParent());
        assertSame(first, childDirectory.getParent());
    }

    @Test
    void aDirectoryCannotContainItself() {
        DirectoryNode root = new DirectoryNode("root");

        assertThrows(IllegalArgumentException.class, () -> root.add(root));
        assertTrue(root.getChildren().isEmpty());
        assertNull(root.getParent());
    }

    @Test
    void aDescendantCannotAdoptItsAncestor() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode middle = new DirectoryNode("middle");
        DirectoryNode leaf = new DirectoryNode("leaf");
        root.add(middle);
        middle.add(leaf);

        assertThrows(IllegalArgumentException.class, () -> leaf.add(root));

        assertEquals(List.of(middle), root.getChildren());
        assertEquals(List.of(leaf), middle.getChildren());
        assertTrue(leaf.getChildren().isEmpty());
        assertNull(root.getParent());
        assertSame(root, middle.getParent());
        assertSame(middle, leaf.getParent());
    }

    @Test
    void nullChildrenAreRejectedWithoutChangingTheDirectory() {
        DirectoryNode root = new DirectoryNode("root");

        assertThrows(IllegalArgumentException.class, () -> root.add(null));
        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    void childrenAreReadOnlySnapshotsAndValidNestingStillWorks() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode nested = new DirectoryNode("nested");
        FileNode file = new FileNode("data.bin", 30);
        nested.add(file);
        root.add(nested);
        List<AbstractNode> snapshot = root.getChildren();

        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        root.add(new DirectoryNode("later"));

        assertEquals(List.of(nested), snapshot);
        assertEquals(2, root.getChildren().size());
        assertEquals(30, new TotalSizeVisitor().calculate(root));
        assertSame(root, nested.getParent());
        assertSame(nested, file.getParent());
    }
}

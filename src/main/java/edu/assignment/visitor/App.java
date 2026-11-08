package edu.assignment.visitor;

import java.util.List;

public final class App {
    public static void main(String[] args) {
        if (args.length > 2) {
            throw new IllegalArgumentException("Usage: App [filenameSuffix] [minimumBytes]");
        }
        String suffix = args.length > 0 ? args[0] : ".txt";
        long minimumSize = args.length > 1 ? Long.parseLong(args[1]) : 100;
        DirectoryNode root = createDemoTree();

        System.out.println("Visitor Pattern - In-memory File System");
        System.out.println("Total size: " + new TotalSizeVisitor().calculate(root) + " bytes");
        SearchVisitor search = new SearchVisitor(name -> name.endsWith(suffix), minimumSize);
        printMatches(search.search(root), suffix, minimumSize);

        StatisticsVisitor statistics = new StatisticsVisitor();
        statistics.analyze(root);
        System.out.println("Files: " + statistics.getFileCount());
        System.out.println("Directories (including root): " + statistics.getDirectoryCount());
        System.out.println("Maximum depth (root = 0): " + statistics.getMaxDepth());
    }

    private static void printMatches(List<String> matches, String suffix, long minimumSize) {
        System.out.println("Search (name ends with \"" + suffix + "\", size >= " + minimumSize + "):");
        if (matches.isEmpty()) {
            System.out.println("  (no matches)");
        }
        for (String path : matches) {
            System.out.println("  " + path);
        }
    }

    static DirectoryNode createDemoTree() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode images = new DirectoryNode("images");
        DirectoryNode documents = new DirectoryNode("docs");
        DirectoryNode archive = new DirectoryNode("archive");

        root.add(new FileNode("notes.txt", 120));
        root.add(images);
        root.add(documents);
        root.add(new DirectoryNode("empty"));
        root.add(new FileNode("data.bin", 1024));
        images.add(new FileNode("logo.png", 500));
        documents.add(new FileNode("manual.txt", 300));
        documents.add(new FileNode("todo.txt", 80));
        documents.add(archive);
        archive.add(new FileNode("old.txt", 200));
        return root;
    }
}

# Visitor Pattern File System

Assignment 06 in-memory file system. Java 17 and Maven.

The model contains Element, FileNode and DirectoryNode. A shared AbstractNode
stores each name and parent. DirectoryNode.add validates ownership, duplicate
names and cycles. Each concrete element dispatches to the Visitor interface.

Compile from the directory containing pom.xml:

```bash
mvn compile
```

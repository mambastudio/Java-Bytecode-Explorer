package com.mamba.bytecodeexplorer.file;

import static com.mamba.bytecodeexplorer.file.FileRef.ExploreType.FILE_EXPLORE;
import static com.mamba.bytecodeexplorer.file.FileRef.ExploreType.FILE_OR_FOLDER_EXPLORE;
import static com.mamba.bytecodeexplorer.file.FileRef.ExploreType.FOLDER_EXPLORE;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public record FileRef(Path path) {

    public enum ExploreType { FILE_EXPLORE, FOLDER_EXPLORE, FILE_OR_FOLDER_EXPLORE }

    private static final boolean IS_WINDOWS =
            System.getProperty("os.name").toLowerCase().contains("win");

    // Compact constructor: normalize if real path
    public FileRef {
        if (path != null) {
            path = path.normalize().toAbsolutePath();
        }
    }

    // Secondary constructors
    public FileRef(String path) {
        this(path == null ? null : Paths.get(path));
    }

    public FileRef(File file) {
        this(file == null ? null : file.toPath());
    }

    public FileRef(URI uri) {
        this(uri == null ? null : new File(uri).toPath());
    }

    public FileRef() {
        this(Paths.get(".").toAbsolutePath());
    }

    // Factory for explicit dummy root
    public static FileRef virtualRoot() {
        return new FileRef((Path) null);
    }

    public boolean isVirtualRoot() {
        return path == null;
    }

    // ------------------
    // File properties
    // ------------------

    public boolean isFileSystem() {
        return !isVirtualRoot() && path.getParent() == null;
    }

    public boolean isLeaf() {
        return !isVirtualRoot() && !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    public boolean isDirectory() {
        return !isVirtualRoot() && Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    public boolean exists() {
        return !isVirtualRoot() && Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    public boolean isDirectoryOrFile(String... extensions) {
        return isDirectory() || isFileExtension(extensions);
    }

    public FileSystem fileSystem() {
        if (isVirtualRoot()) throw new IllegalStateException("Virtual root has no file system");
        return path.getFileSystem();
    }

    public String rootName() {
        if (isVirtualRoot()) return "<virtual-root>";
        Path root = path.getRoot();
        return root == null ? path.toString() : root.toString();
    }

    public File file() {
        if (isVirtualRoot()) throw new IllegalStateException("Virtual root has no file");
        return path.toFile();
    }

    public boolean isHidden() {
        return !isVirtualRoot() && file().isHidden();
    }

    // ------------------
    // Hierarchy relations
    // ------------------

    public boolean isAncestorOf(FileRef child) {
        if (isVirtualRoot()) return true;        // virtual root = ancestor of all
        if (child == null || child.isVirtualRoot()) return false;        
        return !this.equals(child) && child.path.startsWith(path);
    }

    public boolean isDescendantOf(FileRef parent) {
        return parent != null && parent.isAncestorOf(this);
    }

    public boolean hasChildren() {
        return isDirectory() && path.toFile().list().length != 0;
    }

    public boolean hasParent() {
        return !isVirtualRoot() && path.getParent() != null;
    }

    public Optional<FileRef> parent() {
        return hasParent() ? Optional.of(new FileRef(path.getParent()))
                           : Optional.empty();
    }

    // ------------------
    // Directory queries
    // ------------------

    public boolean isDirectoryEmpty(String... extensions) {
        if (!isDirectory()) return true;

        boolean filterByExtension = extensions != null && extensions.length > 0;

        try (Stream<Path> stream = Files.list(path)) {
            return stream
                    .map(Path::toFile)
                    .map(FileRef::new)
                    .filter(ref -> {
                        if (ref.isDirectory()) return true;
                        if (!filterByExtension) return true;
                        return ref.hasExtension() &&
                               Arrays.stream(extensions).anyMatch(ref::isFileExtension);
                    })
                    .findFirst()
                    .isEmpty();
        } catch (IOException ex) {
            Logger.getLogger(FileRef.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true; // fail-safe
    }

    public boolean hasFileOfType(String... extensions) {
        if (!isDirectory()) return false;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path,
                p -> Files.isRegularFile(p) && hasMatchingExtension(p, extensions))) {
            return stream.iterator().hasNext();
        } catch (IOException ex) {
            Logger.getLogger(FileRef.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean hasMatchingExtension(Path path, String[] extensions) {
        String fileName = path.getFileName().toString().toLowerCase();
        for (String ext : extensions) {
            if (fileName.endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // ------------------
    // Naming
    // ------------------

    public String name() {
        if (isVirtualRoot()) return "<virtual-root>";

        Path fileName = path.getFileName();
        return fileName == null
                ? path.toString()
                : fileName.toString().replace("/", "");
    }

    public String nameWithoutExtension() {
        String n = name();
        int index = n.lastIndexOf(".");
        return index > 0 ? n.substring(0, index) : n;
    }

    // ------------------
    // File ops
    // ------------------

    public Optional<FileRef> rename(String name) {
        if (isVirtualRoot()) return Optional.empty();
        try {
            return Optional.of(new FileRef(Files.move(path, path.resolveSibling(name), REPLACE_EXISTING)));
        } catch (IOException ex) {
            Logger.getLogger(FileRef.class.getName()).log(Level.SEVERE, null, ex);
            return Optional.empty();
        }
    }

    public String fileExtension() {
        String fileName = name();
        int index = fileName.lastIndexOf(".");
        return index > 0 ? fileName.substring(index) : "";
    }

    public boolean hasExtension() {
        return !isDirectory() && !fileExtension().isEmpty();
    }

    public boolean isFileExtension(String extension) {
        return fileExtension().equals(extension);
    }

    public boolean isFileExtension(String... extensions) {
        return Arrays.asList(extensions).contains(fileExtension());
    }

    public FileRef[] children(boolean includeHidden, ExploreType exploreType, FileExtensions extensions) {
        if (isVirtualRoot()) return new FileRef[0];

        File directory = file();
        File[] files = switch (exploreType) {
            case FILE_EXPLORE -> directory.listFiles();
            case FOLDER_EXPLORE -> directory.listFiles(f -> new FileRef(f).isDirectory());
            case FILE_OR_FOLDER_EXPLORE -> extensions.hasExtensions()
                    ? directory.listFiles(f -> {
                        FileRef ref = new FileRef(f);
                        return ref.isDirectory() || Arrays.stream(extensions.extensions()).anyMatch(ref::isFileExtension);
                    })
                    : directory.listFiles();
        };

        return files == null ? new FileRef[0] :
                Arrays.stream(files)
                        .map(FileRef::new)
                        .filter(ref -> includeHidden || !ref.isHidden())
                        .toArray(FileRef[]::new);
    }

    public FileRef[] children(String... extensions) {
        return children(false, FILE_OR_FOLDER_EXPLORE, new FileExtensions(extensions));
    }

    public FileRef[] children(FileExtensions extensions) {
        return children(false, FILE_OR_FOLDER_EXPLORE, extensions);
    }

    public FileRef[] children(boolean includeHidden, String... extensions) {
        return children(includeHidden, FILE_OR_FOLDER_EXPLORE, new FileExtensions(extensions));
    }

    // ------------------
    // Utilities
    // ------------------

    public String sizeReadable() {
        if (isVirtualRoot()) return "-";

        long bytes = file().length();
        return switch ((int) (Math.log10(bytes == 0 ? 1 : bytes) / 3)) {
            case 0 -> String.format("%1$3.3g  B", (float) bytes);
            case 1 -> String.format("%1$3.3g KB", bytes / 1024f);
            case 2 -> String.format("%1$3.3g MB", bytes / (1024f * 1024f));
            case 3 -> String.format("%1$3.3g GB", bytes / (1024f * 1024f * 1024f));
            case 4 -> String.format("%1$3.3g TB", bytes / (1024f * 1024f * 1024f * 1024f));
            case 5 -> String.format("%1$3.3g PB", bytes / (1024f * 1024f * 1024f * 1024f * 1024f));
            case 6 -> String.format("%1$3.3g EB", bytes / (1024f * 1024f * 1024f * 1024f * 1024f * 1024f));
            default -> "Too large";
        };
    }

    // ------------------
    // Equality & debug
    // ------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FileRef other)) return false;

        if (isVirtualRoot() && other.isVirtualRoot()) return true;
        if (isVirtualRoot() || other.isVirtualRoot()) return false;

        return IS_WINDOWS
                ? path.toString().equalsIgnoreCase(other.path.toString())
                : path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return isVirtualRoot() ? 0 : (
                IS_WINDOWS
                        ? path.toString().toLowerCase(Locale.ROOT).hashCode()
                        : path.hashCode()
        );
    }

    @Override
    public String toString() {
        return name();
    }
}

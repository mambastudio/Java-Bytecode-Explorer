/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.file;

import static com.mamba.bytecodeexplorer.file.FileRef2.ExploreType.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author joemw
 */
public record RealFile(Path path) implements FileRef2 {
    
    // Compact constructor: normalize if real path
    public RealFile {
        Objects.requireNonNull(path, "path is null and is not allowed");            
    }

    // Secondary constructors
    public RealFile(String path) {
        this(path == null ? null : Paths.get(path));
    }

    public RealFile(File file) {
        this(file == null ? null : file.toPath());
    }

    public RealFile(URI uri) {
        this(uri == null ? null : new File(uri).toPath());
    }

    public boolean isLeaf() {
        return !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    public boolean isDirectory() {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    public boolean exists() {
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    public boolean isDirectoryOrFile(String... extensions) {
        return isDirectory() || isFileExtension(extensions);
    }

    public FileSystem fileSystem() {
        return path.getFileSystem();
    }

    public String rootName() {
        Path root = path.getRoot();
        return root == null ? path.toString() : root.toString();
    }

    public File file() {
        return path.toFile();
    }

    public boolean isHidden() {
        return file().isHidden();
    }
    
    // ------------------
    // Hierarchy relations
    // ------------------
    
    public boolean hasChildren() {
        return isDirectory() && path.toFile().list().length != 0;
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

    public Optional<RealFile> rename(String name) {
        try {
            return Optional.of(new RealFile(Files.move(path, path.resolveSibling(name), REPLACE_EXISTING)));
        } catch (IOException ex) {
            Logger.getLogger(RealFile.class.getName()).log(Level.SEVERE, null, ex);
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

    public RealFile[] children(boolean includeHidden, ExploreType exploreType, FileExtensions extensions) {        
        File directory = file();
        File[] files = switch (exploreType) {
            case FILE_EXPLORE -> directory.listFiles();
            case FOLDER_EXPLORE -> directory.listFiles(f -> new FileRef(f).isDirectory());
            case FILE_OR_FOLDER_EXPLORE -> extensions.hasExtensions()
                    ? directory.listFiles(f -> {
                        var ref = new RealFile(f);
                        return ref.isDirectory() || Arrays.stream(extensions.extensions()).anyMatch(ref::isFileExtension);
                    })
                    : directory.listFiles();
        };

        return files == null ? new RealFile[0] :
                Arrays.stream(files)
                        .map(RealFile::new)
                        .filter(ref -> includeHidden || !ref.isHidden())
                        .toArray(RealFile[]::new);
    }

    public RealFile[] children(String... extensions) {
        return children(false, FILE_OR_FOLDER_EXPLORE, new FileExtensions(extensions));
    }

    public RealFile[] children(FileExtensions extensions) {
        return children(false, FILE_OR_FOLDER_EXPLORE, extensions);
    }

    public RealFile[] children(boolean includeHidden, String... extensions) {
        return children(includeHidden, FILE_OR_FOLDER_EXPLORE, new FileExtensions(extensions));
    }

    // ------------------
    // Utilities
    // ------------------

    

    // ------------------
    // Equality & debug
    // ------------------

    
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RealFile other)) return false;

        return IS_WINDOWS
                ? this.path.normalize().toString()
                      .equalsIgnoreCase(other.path.normalize().toString())
                : this.path.equals(other.path);
    }
    
    @Override
    public final int hashCode() {
        return IS_WINDOWS
                ? path.normalize().toString()
                      .toLowerCase(Locale.ROOT)
                      .hashCode()
                : path.hashCode();
    }

    @Override
    public String toString() {
        return name();
    }
    
    @Override
    public boolean isVirtual() {
        return false;
    }
    
}

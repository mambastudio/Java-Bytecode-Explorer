/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher;

import static com.mamba.bytecodeexplorer.watcher.FileRef.ExploreType.FILE_EXPLORE;
import static com.mamba.bytecodeexplorer.watcher.FileRef.ExploreType.FILE_OR_FOLDER_EXPLORE;
import static com.mamba.bytecodeexplorer.watcher.FileRef.ExploreType.FOLDER_EXPLORE;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author user
 */
public record FileRef(Path path) {
    
    public enum ExploreType{FILE_EXPLORE, FOLDER_EXPLORE, FILE_OR_FOLDER_EXPLORE};
    
    private static final boolean IS_WINDOWS =
        System.getProperty("os.name").toLowerCase().contains("win");
    
    public FileRef{
        path = path.normalize().toAbsolutePath();
    }
    
    public FileRef(String path){
        this(Paths.get(path));
    }
    
    public FileRef(File file){
        this(file.toPath());
    }
    
    public FileRef(URI uri){
        this(new File(uri));
    }
    
    public FileRef(){
        this(Paths.get(".").toAbsolutePath());
    }
    
    //is C:, D:, E:, etc
    public boolean isFileSystem(){
        return (path.getParent() == null);
    }
    
    public boolean isLeaf(){
        return !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }
    
    public boolean isDirectory(){
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }
        
    public boolean exists(){
        return path != null && Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }
    
    public boolean isDirectoryOrFile(String... extensions){
        return isDirectory() || isFileExtension(extensions);
    }
        
    public FileSystem fileSystem(){        
        return path.getFileSystem();
    }
    
    public String rootName(){
        return path.getRoot().toString();
    }
    
    public File file(){
        return path.toFile();
    }
    
    public boolean isHidden(){
        return this.file().isHidden();
    }
    
    public boolean isAncestorOf(FileRef child) {
        if(!isDirectory() || path != null)
            return false;
        
        return !this.equals(child) &&
            child.path().startsWith(path());
    }
    
    public boolean isDescendantOf(FileRef parent) {
        return parent.isAncestorOf(this);
    }
    
    public boolean hasChildren(){
        if(!isDirectory()) return false;
        return path.toFile().list().length != 0;
    }
    
    public boolean hasParent(){
        return path.getParent() != null;
    }
    
    public Optional<FileRef> parent(){
        return switch(hasParent()){
            case true -> Optional.of(new FileRef(path.getParent()));
            case false -> Optional.empty();
        };
    }
    
    public boolean isDirectoryEmpty(String... extensions){
        if (!isDirectory()) return true;

        boolean filterByExtension = extensions != null && extensions.length > 0;

        try (Stream<Path> stream = Files.list(path)) {
            return stream
                    .map(Path::toFile)
                    .map(FileRef::new)
                    .filter(ref -> {
                        if (ref.isDirectory()) {
                            return true; // folders always count
                        }

                        // File case
                        if (!filterByExtension) {
                            return true; // any file counts
                        }

                        return ref.hasExtension() &&
                               Arrays.stream(extensions).anyMatch(ref::isFileExtension);
                    })
                    .findFirst()
                    .isEmpty(); // true = nothing matched, so directory is "empty"
        } catch (IOException ex) {
            Logger.getLogger(FileRef.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true; // fail-safe: assume empty if error
    }
    
    public boolean hasFileOfType(String... extensions){
        if(!isDirectory())
            return false;
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path,
                p -> Files.isRegularFile(path) && hasMatchingExtension(path, extensions))) {
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
    
    public String name(){        
        return switch (path.getFileName()) {
            case null -> {
                Path root = Paths.get(path.getFileSystem().toString());
                yield root.getFileName().toString();
            }
            default -> path.getFileName().toString().replace("/", "");
        };
    }
    
    public String nameWithoutExtension(){
        return switch(!hasExtension()){
            case true -> name();
            case false ->{
                String fileName = name();
                int index = fileName.lastIndexOf(".");
                yield fileName.substring(0, index);
            }
        };        
    }
    
    public Optional<FileRef> rename(String name) {
        try {
            return Optional.of(new FileRef(Files.move(path, path.resolveSibling(name), REPLACE_EXISTING)));
        } catch (IOException ex) {
            Logger.getLogger(FileRef.class.getName()).log(Level.SEVERE, null, ex);
            return Optional.empty();
        }
    }
    
    public String fileExtension(){
        String fileName = name();
        int index = fileName.lastIndexOf(".");
        
        return switch(index > 0){
            case true -> fileName.substring(index);
            case false -> "";
        };
    }
    
    public boolean hasExtension(){       
        return switch(isDirectory()){
            case true -> false;
            case false -> !fileExtension().isEmpty();
        };
    }
    
    public boolean isFileExtension(String extension){
        return (fileExtension() != null) && (fileExtension().equals(extension));
    }
    
    public boolean isFileExtension(String... extension){
        for(String ext : extension)
            if(ext.equals(fileExtension()))
                return true;
        return false;
    }
   
    public FileRef[] children(boolean includeHidden, ExploreType exploreType, FileExtensions extensions){
        File[] files = null;
       
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            File directory = file();

            files = switch (exploreType) {
                case FILE_EXPLORE -> directory.listFiles(); // return all files and folders
                case FOLDER_EXPLORE -> directory.listFiles(file -> new FileRef(file).isDirectory()); //return all folders
                case FILE_OR_FOLDER_EXPLORE -> { //return all folders and specific file extensions
                    yield switch (extensions.hasExtensions()) {
                        case true -> directory.listFiles((var file) -> {
                            FileRef fo = new FileRef(file);
                            return fo.isDirectory() || Arrays.stream(extensions.extensions()).anyMatch(fo::isFileExtension);
                        });
                        case false -> directory.listFiles(); // all files and folders
                    };
                }                
            };            
        }
        
        return switch (files) {
                    case null -> new FileRef[0];
                    case File[] fs when fs.length > 0 -> 
                                    Arrays.stream(fs)
                                          .map(FileRef::new)
                                          .filter(ref -> includeHidden || !ref.isHidden())
                                          .toArray(FileRef[]::new);
                    default -> new FileRef[0]; // handles null and empty arrays
                };  
    } 
    
    public FileRef[] children(String... extensions){
        return children(false, FILE_OR_FOLDER_EXPLORE, new FileExtensions(extensions));
    }
    
    public FileRef[] children(FileExtensions extensions){
        return children(false, FILE_OR_FOLDER_EXPLORE, extensions);
    }
    
    public FileRef[] children(boolean includeHidden, String... extensions){
        return children(includeHidden, FILE_OR_FOLDER_EXPLORE, new FileExtensions(extensions));
    }
    
    public String sizeReadable() {
        long bytes = file().length();

        return switch (bytes) {
            case long b when b < 1_000L -> String.format("%1$3.3g  B", (float) b);
            case long b when b < 1024L * 1024L -> String.format("%1$3.3g KB", b / 1024f);
            case long b when b < 1024L * 1024L * 1024L -> String.format("%1$3.3g MB", b / (1024f * 1024f));
            case long b when b < 1024L * 1024L * 1024L * 1024L -> String.format("%1$3.3g GB", b / (1024f * 1024f * 1024f));
            case long b when b < 1024L * 1024L * 1024L * 1024L * 1024L -> String.format("%1$3.3g TB", b / (1024f * 1024f * 1024f * 1024f));
            case long b when b < 1024L * 1024L * 1024L * 1024L * 1024L * 1024L -> String.format("%1$3.3g PB", b / (1024f * 1024f * 1024f * 1024f * 1024f));
            case long b when b < Long.MAX_VALUE -> String.format("%1$3.3g EB", b / (1024f * 1024f * 1024f * 1024f * 1024f * 1024f));
            default -> "Too large";
        };
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FileRef other)) return false;
        
        return switch(IS_WINDOWS){
            case true -> path.normalize().toString().equalsIgnoreCase(other.path.normalize().toString());
            case false -> path.normalize().equals(other.path.normalize());
        };
    }

    @Override
    public int hashCode() {        
        return switch(IS_WINDOWS){
            case true -> path.normalize().toString().toLowerCase(Locale.ROOT).hashCode();
            case false -> path.normalize().hashCode();
        };
    }

    @Override
    public String toString(){
        return name();
    }
}

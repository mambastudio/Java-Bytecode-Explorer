package com.mamba.bytecodeexplorer.watcher.treeitem;

import com.mamba.bytecodeexplorer.RecursiveTreeModel;
import com.mamba.bytecodeexplorer.watcher.FileExtensions;
import com.mamba.bytecodeexplorer.watcher.FileRef;
import com.mamba.bytecodeexplorer.watcher.FileRefTree;
import java.nio.file.Path;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FileRefModel implements FileRefTree<FileRefModel>, RecursiveTreeModel<FileRef, FileRefModel> {

    private final FileRef ref;
    private final FileExtensions extensionsHolder;

    private ObservableList<FileRefModel> children = null; // lazy

    public FileRefModel(FileRef ref, FileExtensions extensions) {
        this.ref = ref;
        this.extensionsHolder = extensions;
    }
    
    public FileRefModel(FileRef ref) {
        this(ref, new FileExtensions());
    }

    public FileRefModel(FileRef ref, String extension) {
        this(ref, new FileExtensions(extension));
    }
    
    public FileRefModel(FileRef ref, FileRef child, String extension) {
        this(ref, child, new FileExtensions(extension));
    }
    
    public FileRefModel(FileRef ref, FileRef child, FileExtensions extensions) {
        this(ref, extensions);
        this.children = FXCollections.observableArrayList();
        this.children.add(new FileRefModel(child, extensions));
    }

    public FileRefModel(Path path, String extension) {
        this(new FileRef(path), new FileExtensions(extension));
    }

    public FileRefModel(String url, String extension) {
        this(new FileRef(url), new FileExtensions(extension));
    }

    @Override
    public FileRef ref() {
        return ref;
    }

    @Override
    public ObservableList<FileRefModel> children() {
        if (ref == null) return FXCollections.emptyObservableList();

        if (children == null) {
            children = FXCollections.observableArrayList();

            for (FileRef childRef : ref.children()) {
                if (childRef.isDirectory() ||
                    (extensionsHolder.hasExtensions() && childRef.isFileExtension(extensionsHolder.extensions()))) {
                    children.add(new FileRefModel(childRef, extensionsHolder));
                }
            }
        }

        return children;
    }
    
    public ObservableList<FileRefModel> children2(FileRefModel m){
        return null;
    }

    //CANT THIS BE TAKEN TO FileRefTree interface instead?
    @Override
    public Optional<FileRefModel> findInTree(FileRef target) {
        //check node ref for this is equal to target: SHOULD WE MODIFY equals AND USE IT INSTEAD?
        if (ref != null && ref.path().normalize().equals(target.path().normalize()))
            return Optional.of(this);

        //check if children has target: SHOULD THIS BE DELETED AND USE ONE BELOW INSTEAD?
        for (FileRefModel child : children()) {
            if (child.ref != null && child.ref.path().normalize().equals(target.path().normalize()))
                return Optional.of(child);
        }

        //LOOKS STABLE TO USE THIS INSTEAD OF THE ABOVE
        for (FileRefModel child : children()) {
            Optional<FileRefModel> match = child.findInTree(target);
            if (match.isPresent()) return match;
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return ref != null ? ref.name() : "";
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj ||
            (obj instanceof FileRefModel other && ref != null && ref.equals(other.ref));
    }

    @Override
    public int hashCode() {
        return ref != null ? ref.hashCode() : 0;
    }
}

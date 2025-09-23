package com.mamba.bytecodeexplorer.watcher.treeitem;

import com.mamba.bytecodeexplorer.watcher.AbstractFileRefTree;
import com.mamba.bytecodeexplorer.watcher.FileExtensions;
import com.mamba.bytecodeexplorer.watcher.FileRef;
import java.nio.file.Path;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FileRefModel extends AbstractFileRefTree<FileRefModel>{

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
    
    @Override
    public String toString() {
        return ref != null ? ref.name() : "";
    }   
}

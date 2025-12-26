package com.mamba.bytecodeexplorer.tree.model;

import com.mamba.bytecodeexplorer.core.AbstractFileRefTree;
import com.mamba.bytecodeexplorer.file.FileExtensions;
import com.mamba.bytecodeexplorer.file.type.FileRef;
import com.mamba.bytecodeexplorer.file.type.RealFile;
import com.mamba.bytecodeexplorer.file.type.VirtualFile;
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
        this(new RealFile(path), new FileExtensions(extension));
    }

    public FileRefModel(String url, String extension) {
        this(new RealFile(url), new FileExtensions(extension));
    }

    @Override
    public FileRef ref() {
        return ref;
    }

    @Override
    public ObservableList<FileRefModel> children() {
        if (ref == null) return FXCollections.emptyObservableList();
        
        if(ref instanceof VirtualFile)
            children = FXCollections.observableArrayList();
        
        if (children == null) {
            children = FXCollections.observableArrayList();
            
            if(!(ref instanceof RealFile f))
                return children;
            
            for (RealFile childRef : f.children()) {
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

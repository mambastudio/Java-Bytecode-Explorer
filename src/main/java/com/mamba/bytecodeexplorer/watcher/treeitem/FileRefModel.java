package com.mamba.bytecodeexplorer.watcher.treeitem;

import com.mamba.bytecodeexplorer.watcher.FileExtensions;
import com.mamba.bytecodeexplorer.watcher.FileRef;
import java.nio.file.Path;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FileRefModel {

    private final FileRef ref;
    private final FileExtensions extensionsHolder;

    private ObservableList<FileRefModel> children = null; // lazy

    public FileRefModel(FileRef ref, FileExtensions extensions) {
        this.ref = ref;
        this.extensionsHolder = extensions;
    }

    public FileRefModel(FileRef ref, String... extensions) {
        this(ref, new FileExtensions(extensions));
    }

    public FileRefModel(Path path, String... extensions) {
        this(new FileRef(path), new FileExtensions(extensions));
    }

    public FileRefModel(String url, String... extensions) {
        this(new FileRef(url), new FileExtensions(extensions));
    }

    public FileRef getRef() {
        return ref;
    }

    public ObservableList<FileRefModel> getChildren() {
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

    public Optional<FileRefModel> findInTree(FileRef target) {
        if (ref != null && ref.path().normalize().equals(target.path().normalize()))
            return Optional.of(this);

        for (FileRefModel child : getChildren()) {
            if (child.ref != null && child.ref.path().normalize().equals(target.path().normalize()))
                return Optional.of(child);
        }

        for (FileRefModel child : getChildren()) {
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

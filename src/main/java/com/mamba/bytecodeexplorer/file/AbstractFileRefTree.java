package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.file.type.RealFile;

public abstract class AbstractFileRefTree<Y extends FileRefTree<Y>>
        implements FileRefTree<Y> {

    public boolean isDirectory(){
        if(!(ref() instanceof RealFile f))
            return false;
        return f.isDirectory();
    }

    public boolean isLeaf(){
        if(!(ref() instanceof RealFile f))
            return false;
        return f.isLeaf();
    }

    public boolean isTerminal(){
        if(!(ref() instanceof RealFile f))
            return false;
        return f.isLeaf();
    }

    public boolean isResolved() {
        return ref() != null;
    }

    @Override
    public final boolean equals(Object obj) {
        return  this == obj ||
                (obj instanceof FileRefTree<?> other &&
                ref() != null && ref().equals(other.ref()));
    }

    @Override
    public final int hashCode() {
        return ref() != null ? ref().hashCode() : 0;
    }

    @Override
    public String toString(){
        return this.ref().name();
    }
}

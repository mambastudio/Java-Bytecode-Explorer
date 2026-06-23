package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.file.type.FileRef;
import java.util.Objects;
import java.util.function.Supplier;

public interface FileRefTree<Y extends FileRefTree<Y>> extends Tree<FileRef, Y>{

    default boolean isDescendantOf(Y parent){
        return ref().isDescendantOf(parent.ref());
    }

    default boolean isAncestorOf(Y child) {
        return ref().isAncestorOf(child.ref());
    }

    @Override
    default boolean childrenContain(FileRef child){
        for(Y y : children()){
            if(y.ref().equals(child))
                return true;
        }
        return false;
    }

    @Override
    default TreeRelation<FileRef, Y> findInTree(FileRef target) {
        return FileRefTree.this.findInTree(target, null);
    }

    default TreeRelation<FileRef, Y> findInTree(FileRef target, FileRef parent) {
        if (target == null)
            return TreeRelation.empty();

        if (ref() != null && ref().equals(target)) {
            return parent != null
                ? new TreeRelation(this, parent, target)
                : TreeRelation.<FileRef, Y>asChild((Y)this, target);
        }

        if (!target.isDescendantOf(ref()))
            return TreeRelation.empty();

        for (var child : this.children()) {
            var match = child.findInTree(target, ref());
            if (match.isPresent()) return match;
        }

        return TreeRelation.empty();
    }

    @Override
    default boolean remove(FileRef target){
        if(target == null)
            return false;

        if (ref() != null && ref().equals(target))
            return false;

        if(!target.isDescendantOf(ref()))
            return false;

        if(removeChild(target))
            return true;

        for (var child : children()) {
            boolean removed = child.remove(target);
            if (removed) return true;
        }

        return false;
    }

    default boolean addInParent(FileRef parent, Supplier<Y> function){
        Objects.requireNonNull(function);

        if(parent == null)
            return false;

        if (ref() != null && ref().equals(parent)){
            this.children().add(function.get());
            return true;
        }

        if(!parent.isDescendantOf(ref()))
            return false;

        for (var child : children()) {
            boolean added = child.addInParent(parent, function);
            if (added) return true;
        }

        return false;
    }

    default boolean remove(Y y){
        if(exist(y))
            throw new UnsupportedOperationException("method remove not yet supported");
        return false;
    }

    default String treeString(int indent){
        var s = "";
        if(hasChildren()){
            for(Y y : children()){
                s = s.concat(y.toString()).concat("\n");
                if(y.hasChildren()){
                    s = s.concat(y.treeString(indent));
                }
            }
        }
        return s.indent(indent);
    }

    default String treeString(){
        return toString().concat("\n").concat(treeString(3)).stripTrailing();
    }
}

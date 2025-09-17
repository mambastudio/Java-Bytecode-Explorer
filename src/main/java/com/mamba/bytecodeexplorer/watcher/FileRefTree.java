/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher;

import com.mamba.bytecodeexplorer.Tree;
import java.util.Optional;

/**
 *
 * @author user
 * @param <Y>
 */
public interface FileRefTree<Y extends FileRefTree<Y>> extends Tree<FileRef, Y>{
    
    @Override
    default Optional<Y> findInTree(FileRef target) {
        //check node ref for this is equal to target
        if (ref() != null && ref().equals(target))
            return Optional.of((Y)this);
        
        for (Y child : children()) {
            Optional<Y> match = child.findInTree(target);
            if (match.isPresent()) return match;
        }

        return Optional.empty();
    }
    
    default boolean equalsByRef(Object obj) {
        return this == obj ||
            (obj instanceof FileRefTree<?> other &&
             ref() != null && ref().equals(other.ref()));
    }

    default int hashCodeByRef() {
        return ref() != null ? ref().hashCode() : 0;
    }
}

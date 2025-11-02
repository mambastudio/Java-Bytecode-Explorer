/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.core;

import com.mamba.bytecodeexplorer.file.FileRef;
import java.util.Optional;

/**
 *
 * @author user
 * @param <Y>
 */
public interface FileRefTree<Y extends FileRefTree<Y>> extends Tree<FileRef, Y>{
    
    default boolean isDescendantOf(Y parent){
        return ref().isDescendantOf(parent.ref());
    }
    
    default boolean isAncestorOf(Y child) {
        return ref().isAncestorOf(child.ref());
    }
    
    @Override
    default Optional<Y> findInTree(FileRef target) {
        if(target == null)
            return Optional.empty();
        
        //check node ref for this is equal to target
        if (ref() != null && ref().equals(target))
            return Optional.of((Y)this);
        
        //if not descendant, no point of searching deeper
        if(!target.isDescendantOf(ref()))
            return Optional.empty();
        
        for (Y child : children()) {
            var match = child.findInTree(target);
            if (match.isPresent()) return match;
        }

        return Optional.empty();
    } 
    
    
    
    default boolean remove(Y y){
        if(exist(y))
            throw new UnsupportedOperationException("method remove not yet supported");
        return false;
    }
    
    default boolean remove(FileRef ref){
        if(exist(ref))
            throw new UnsupportedOperationException("method remove not yet supported");
        return false;
    }
}

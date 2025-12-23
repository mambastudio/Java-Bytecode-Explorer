/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.core;

import com.mamba.bytecodeexplorer.file.type.FileRef;
import com.mamba.bytecodeexplorer.file.type.RealFile;

/**
 *
 * @author user
 * @param <Y>
 */
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
    public Relation<FileRef> findInTree2(FileRef target) {        
        if(target == null)
            return Relation.empty();
        
        //check node ref for this is equal to target
        if (ref() != null && ref().equals(target))
            return Relation.asChild(target);
                
        //if not descendant, no point of searching deeper
        if(!target.isDescendantOf(ref()))
            return Relation.empty();              
        
        //check if children have any match and return
        if(childrenContain(target))
            return new Relation<>(this.ref(), target);
             
        //go next depth
        for (var child : children()) {
            var match = child.findInTree2(target);
            if (match.isPresent()) return match;
        }

        return Relation.empty();
    }   
    
    @Override
    public boolean remove(FileRef target){
        if(target == null)
            return false;
        
        //check node ref for this is equal to target
        if (ref() != null && ref().equals(target))
            return false;
        
        //if not descendant, no point of searching deeper
        if(!target.isDescendantOf(ref()))
            return false;
        
        //check if children have been removed
        if(removeChild(target))
            return true;         
        
        //if not removed, go to next depth
        for (var child : children()) {
            boolean removed = child.remove(target);
            if (removed) return true;
        }

        return false;
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
        var ch = this.ref().name();
        /*
        if(!children().isEmpty())
            ch = this.ref().name()+ " " +children().toString();       
        */
        return ch;
    }
    
    
}

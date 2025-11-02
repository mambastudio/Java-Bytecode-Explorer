/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.core;

/**
 *
 * @author user
 * @param <Y>
 */
public abstract class AbstractFileRefTree<Y extends FileRefTree<Y>>
        implements FileRefTree<Y> {
    
    public boolean isDirectory(){        
        return ref().isDirectory();
    }
    
    public boolean isLeaf(){
        return ref().isLeaf();
    }
    
    @Override
    public Relation<Y> findInTree2(Y target) {
        if(target == null)
            return Relation.empty();
        
        //check node ref for this is equal to target
        if (ref() != null && ref().equals(target))
            return Relation.asChild(target);
        
        //if not descendant, no point of searching deeper
        if(!target.ref().isDescendantOf(ref()))
            return Relation.empty();
        
        //check if children have any match and return
        if(children().contains(target))
            return new Relation(this, target);
        
        //go next depth
        for (var child : children()) {
            var match = child.findInTree2(target);
            if (match.isPresent()) return match;
        }

        return Relation.empty();
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

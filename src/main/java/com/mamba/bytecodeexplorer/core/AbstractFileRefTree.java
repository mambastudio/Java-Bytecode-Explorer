/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.core;

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

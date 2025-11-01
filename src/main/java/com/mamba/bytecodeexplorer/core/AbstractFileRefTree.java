/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.core;

import com.mamba.bytecodeexplorer.file.FileRef;

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

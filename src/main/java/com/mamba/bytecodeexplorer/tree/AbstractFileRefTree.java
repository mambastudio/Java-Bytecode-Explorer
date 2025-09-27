/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.tree;

/**
 *
 * @author user
 * @param <Y>
 */
public abstract class AbstractFileRefTree<Y extends FileRefTree<Y>>
        implements FileRefTree<Y> {

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FileRefTree<?> other)) return false;
        return ref() != null && ref().equals(other.ref());
    }

    @Override
    public final int hashCode() {
        return ref() != null ? ref().hashCode() : 0;
    }
    
    @Override
    public String toString(){
        String ch = this.ref().name();
        /*
        if(!children().isEmpty())
            ch = this.ref().name()+ " " +children().toString();       
        */
        return ch;
    }
}

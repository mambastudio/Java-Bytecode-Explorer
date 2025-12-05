/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.core;

/**
 *
 * @author joemw
 */
public record Relation<T> (T parent, T child){
    private Relation(){
        this(null, null);
    }
    
    public static<T> Relation<T> empty(){
        return new Relation();
    }
    
    public static<T> Relation<T> asChild(T child){
        return new Relation(null, child);
    }
    
    public boolean hasParent(){
        return parent() != null;
    }
        
    public boolean isPresent(){
        return child != null;
    }
}
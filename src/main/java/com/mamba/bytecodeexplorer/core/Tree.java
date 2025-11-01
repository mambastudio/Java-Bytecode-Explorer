/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.core;

import java.util.Optional;
import javafx.collections.ObservableList;

/**
 *
 * @author user
 * @param <X>
 * @param <Y>
 */
public interface Tree<X, Y extends Tree<X,Y>> {
    X ref();
    ObservableList<Y> children();   
    
    Optional<Y> findInTree(X x);    
    default Optional<Y> findInTree(Y y){
        return findInTree(y.ref());
    }
    
    default Relation<Y> findInTree2(Y y){
        return null;
    }
    
    default Relation<Y> findInTree2(X x){
        return null;
    } 
    
    default boolean exist(Y y){
        var result = findInTree(y);
        return result.isPresent();
    }
    default boolean exist(X x){
        var result = findInTree(x);
        return result.isPresent();
    }
    
    default boolean hasChildren(){
        return !children().isEmpty();
    }
}

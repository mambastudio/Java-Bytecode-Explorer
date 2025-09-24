/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.tree;

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
}

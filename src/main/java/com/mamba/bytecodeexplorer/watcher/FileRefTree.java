/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher;

import javafx.collections.ObservableList;

/**
 *
 * @author user
 * @param <T>
 */
public interface FileRefTree<T extends FileRefTree<T>> {
    FileRef ref();
    ObservableList<T> children();
    default boolean isLeaf(){
        return children().isEmpty();
    }
}

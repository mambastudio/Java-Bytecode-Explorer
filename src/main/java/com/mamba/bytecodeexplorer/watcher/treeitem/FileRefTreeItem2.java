/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher.treeitem;

import com.mamba.bytecodeexplorer.RecursiveTreeItem;
import com.mamba.bytecodeexplorer.watcher.FileRefTree;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.util.Callback;

/**
 *
 * @author user
 * @param <T>
 */
public class FileRefTreeItem2<T extends FileRefTree<T>>  extends RecursiveTreeItem<T> {

    public FileRefTreeItem2(T value,
                            Callback<T, Node> graphicsFactory,
                            Callback<T, ObservableList<T>> childrenFactory) {
        super(value, graphicsFactory, childrenFactory);
    }
}

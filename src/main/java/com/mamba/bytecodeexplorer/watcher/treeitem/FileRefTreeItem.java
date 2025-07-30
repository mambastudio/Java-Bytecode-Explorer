/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher.treeitem;

import com.mamba.bytecodeexplorer.RecursiveTreeItem;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.util.Callback;

/**
 *
 * @author user
 */
public class FileRefTreeItem extends RecursiveTreeItem<FileRefModel>{
    
    public FileRefTreeItem(Callback<FileRefModel, ObservableList<FileRefModel>> childrenFactory) {
        super(childrenFactory);
    }
    
    public FileRefTreeItem(FileRefModel value, Callback<FileRefModel, ObservableList<FileRefModel>> childrenFactory){
        super(value, (item) -> null, childrenFactory);
    }

    public FileRefTreeItem(FileRefModel value, Callback<FileRefModel, Node> graphicsFactory, Callback<FileRefModel, ObservableList<FileRefModel>> childrenFactory){
        super(value, graphicsFactory, childrenFactory);
    }
}

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
public class FileRefInfoTreeItem extends RecursiveTreeItem<FileRefInfo>{
    
    public FileRefInfoTreeItem(Callback<FileRefInfo, ObservableList<FileRefInfo>> childrenFactory) {
        super(childrenFactory);
    }
    
    public FileRefInfoTreeItem(FileRefInfo value, Callback<FileRefInfo, ObservableList<FileRefInfo>> childrenFactory){
        super(value, (item) -> null, childrenFactory);
    }

    public FileRefInfoTreeItem(FileRefInfo value, Callback<FileRefInfo, Node> graphicsFactory, Callback<FileRefInfo, ObservableList<FileRefInfo>> childrenFactory){
        super(value, graphicsFactory, childrenFactory);
    }
}

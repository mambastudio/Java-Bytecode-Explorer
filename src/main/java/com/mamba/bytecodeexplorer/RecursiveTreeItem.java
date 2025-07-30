/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import java.io.IO;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;

/**
 *
 * @author user
 * @param <T>
 */
public class RecursiveTreeItem<T> extends TreeItem<T> {

    private Callback<T, ObservableList<T>> childrenFactory;
    private Callback<T, Node> graphicsFactory;
    private boolean childrenLoaded = false;

    public RecursiveTreeItem(Callback<T, ObservableList<T>> childrenFactory){
        this(null, childrenFactory);
    }

    public RecursiveTreeItem(final T value, Callback<T, ObservableList<T>> childrenFactory){
        this(value, (item) -> null, childrenFactory);
    }

    public RecursiveTreeItem(T value, Callback<T, Node> graphicsFactory, Callback<T, ObservableList<T>> childrenFactory) {
        super(value, graphicsFactory.call(value));
        this.graphicsFactory = graphicsFactory;
        this.childrenFactory = childrenFactory;
    }
    
    @Override
    public ObservableList<TreeItem<T>> getChildren() {
        if (!childrenLoaded) {
            childrenLoaded = true;
            loadChildren();            
        }
        return super.getChildren();
    }
    
    @Override
    public boolean isLeaf() {
        final ObservableList<T> children = childrenFactory.call(getValue());
        
        return children == null || children.isEmpty();
    }
    
    private void loadChildren() {
        final ObservableList<T> children = childrenFactory.call(getValue());

        if (children == null) return;

        children.forEach(child -> {            
            super.getChildren().add(new RecursiveTreeItem<>(child, graphicsFactory, childrenFactory));            
        });

        children.addListener((ListChangeListener<T>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(t ->{
                        RecursiveTreeItem.this.getChildren().add(new RecursiveTreeItem<>(t, graphicsFactory, childrenFactory));
                    });
                }

                if (change.wasRemoved()) {
                    change.getRemoved().forEach(t -> {
                        final List<TreeItem<T>> itemsToRemove = RecursiveTreeItem.this
                            .getChildren()
                            .stream()
                            .filter(treeItem -> treeItem.getValue().equals(t))
                            .collect(Collectors.toList());

                        RecursiveTreeItem.this.getChildren().removeAll(itemsToRemove);
                    });
                }
            }
        });
    }
}

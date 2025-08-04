/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import com.mamba.bytecodeexplorer.utility.ListChangeCase;
import com.mamba.bytecodeexplorer.utility.ListChangeCase.AddRemoveCase;
import com.mamba.bytecodeexplorer.utility.ListChangeCase.Kind;
import com.mamba.bytecodeexplorer.utility.ListChangeCase.PermutationCase;
import com.mamba.bytecodeexplorer.utility.ListChangeCase.UpdateCase;
import com.mamba.bytecodeexplorer.utility.ListChangeUtils;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
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
       
        children.addListener(ListChangeUtils.asListener(change -> {
            switch(change){             
                case AddRemoveCase (Change<? extends T> c, Kind k) -> {
                    switch(k){
                        case ADDED ->{
                            c.getAddedSubList().forEach(t ->{
                                this.getChildren().add(new RecursiveTreeItem<>(t, graphicsFactory, childrenFactory));
                            });
                        }
                        case REMOVED ->{
                            c.getRemoved().forEach(t -> {
                                var itemsToRemove = this
                                    .getChildren()
                                    .stream()
                                    .filter(treeItem -> treeItem.getValue().equals(t))
                                    .collect(Collectors.toList());

                                this.getChildren().removeAll(itemsToRemove);
                            });
                        }
                    }
                }
                default -> {}
            }
        }));
    }
}

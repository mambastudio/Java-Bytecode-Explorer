/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import com.mamba.bytecodeexplorer.tree.item.RecursiveTreeItem;
import com.mamba.bytecodeexplorer.tree.Tree;
import java.util.function.Function;
import javafx.scene.Node;
import javafx.util.Callback;

/**
 *
 * @author user
 * @param <S>
 * @param <Q>
 */
public class TreeItemUtility<S extends Tree<?, S>,  Q extends RecursiveTreeItem<S>> {
    private final S rootModel;
    private final Q rootTreeItem;

    public TreeItemUtility(S rootModel,  Function<S, Q> itemFactory) {
        this.rootModel = rootModel;
        this.rootTreeItem = itemFactory.apply(rootModel);
    }

    public S rootModel() {
        return rootModel;
    }

    public Q rootTreeItem() {
        return rootTreeItem;
    }
    
    public void setExpanded(boolean expanded){
        rootTreeItem.setExpanded(expanded);
    }

    public static <S extends Tree<?, S>, Q extends RecursiveTreeItem<S>> TreeItemUtility<S, Q> of(
            S model,
            Function<S, Q> itemFactory) {
        return new TreeItemUtility<>(model, itemFactory);
    }
    
    public static <S extends Tree<?, S>, Q extends RecursiveTreeItem<S>> TreeItemUtility<S, Q> of(
            S model,
            Callback<S, Node> graphicsFactory,
            Function<S, Q> itemFactory) {
        return new TreeItemUtility<>(model, itemFactory);
    }
}

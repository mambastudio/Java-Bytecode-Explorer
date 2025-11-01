/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.tree.item;

import com.mamba.bytecodeexplorer.core.FileRefTree;
import com.mamba.bytecodeexplorer.core.Tree;
import java.util.function.Function;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

/**
 *
 * @author user
 * @param <S>
 * @param <Q>
 */
public class RootTreeItem<S extends Tree<?, S>,  Q extends RecursiveTreeItem<S>> {
    private final S rootModel;
    private final Q rootTreeItem;

    private RootTreeItem(S rootModel,  Function<S, Q> itemFactory) {
        this.rootModel = rootModel;
        this.rootTreeItem = itemFactory.apply(rootModel);
    }
    
    public S rootModel() {
        return rootModel;
    }

    public Q rootTreeItem() {
        return rootTreeItem;
    }
    
    public TreeItem<S> withNullRootItem(){
        var root = new TreeItem<S>(null);
        root.getChildren().add(rootTreeItem);
        return root;
    }
    
    public void setExpanded(boolean expanded){
        rootTreeItem.setExpanded(expanded);
    }
    
    public RootTreeItem<S,  Q> expandAll(){
        expandRecursive(rootTreeItem);
        return this;
    }
    
    private void expandRecursive(TreeItem<S> treeItem){
        treeItem.setExpanded(true); 
        for(TreeItem<S> item : treeItem.getChildren())
            expandRecursive(item);
    }

    public static <S extends Tree<?, S>, Q extends RecursiveTreeItem<S>> RootTreeItem<S, Q> of(
            S model,
            Function<S, Q> itemFactory) {
        return new RootTreeItem<>(model, itemFactory);
    }
    
    public static <S extends Tree<?, S>, Q extends RecursiveTreeItem<S>> RootTreeItem<S, Q> of(
            S model,
            Callback<S, Node> graphicsFactory,
            Function<S, Q> itemFactory) {
        return new RootTreeItem<>(model, itemFactory);
    }
    
    public static<S extends FileRefTree<S>> RootTreeItem<S, FileRefTreeItem<S>> ofFileRef(S model, Callback<S, Node> graphicsFactory){        
        return new RootTreeItem<>(
                model,
                m -> new FileRefTreeItem<S>(
                    m,
                    graphicsFactory,
                    S::children // no parentheses here
                ));
    }
    
    public static<S extends FileRefTree<S>> RootTreeItem<S, FileRefTreeItem<S>> ofFileRef(S model){
        Callback<S, Node> graphicsFactory = fileRef -> {
            StackedFontIcon fontIcon = new StackedFontIcon();            
            if(fileRef.ref().isDirectory() && fileRef.ref().isDirectoryEmpty(".class")){
                FontIcon icon = new FontIcon("mdal-folder");
                fontIcon.getChildren().add(icon);
            }
            else if(fileRef.ref().isDirectory()){
                FontIcon icon = new FontIcon("mdoal-create_new_folder");
                fontIcon.getChildren().add(icon);                
            }
            else if(!fileRef.ref().isDirectory()){
                FontIcon icon = new FontIcon("mdoal-code");
                fontIcon.getChildren().add(icon);
            }
            return fontIcon;
        };
        
        return ofFileRef(model, graphicsFactory);
    }
}

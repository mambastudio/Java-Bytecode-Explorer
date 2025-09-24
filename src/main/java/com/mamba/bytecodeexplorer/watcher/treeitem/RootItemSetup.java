/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher.treeitem;

import com.mamba.bytecodeexplorer.tree.item.FileRefTreeItem;
import com.mamba.bytecodeexplorer.tree.FileRefTree;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

/**
 *
 * @author user
 * @param <S>
 */
public class RootItemSetup<S extends FileRefTree<S>> {    
    private final S rootModel;
    private final TreeItem<S> rootItem = new TreeItem(null); // acts like an invisible virtual rootItem TODO: Help in creating a virtual fileref (points to nothing) to avoid nulls

    private final Callback<S, Node> graphicsFactory = fileRef -> {
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
    
    public RootItemSetup(S rootModel){
        this.rootModel = rootModel;
        
        //this should be called if folder is added in rootItem (children and their whole hierarchy of subchildren are added automatically in the listener or during initialisation)
        rootItem.getChildren().add(
            new FileRefTreeItem<>(
                this.rootModel,
                this.graphicsFactory,
                S::children // no parentheses here
            )
        );  
    }    
    
    public S rootModel(){
        return rootModel;
    }
    
    public TreeItem<S> rootItem(){
        return rootItem;
    }
}

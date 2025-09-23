/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher.treeitem;

import com.mamba.bytecodeexplorer.watcher.FileRefTree;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

/**
 *
 * @author user
 * @param <T>
 */
public class RootItemSetup<T extends FileRefTree<T>> {    
    private final T rootModel;
    private final TreeItem<T> rootItem = new TreeItem(null); // acts like an invisible virtual rootItem TODO: Help in creating a virtual fileref (points to nothing) to avoid nulls

    private final Callback<T, Node> graphicsFactory = fileRef -> {
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
    
    public RootItemSetup(T rootModel){
        this.rootModel = rootModel;
        
        //this should be called if folder is added in rootItem (children and their whole hierarchy of subchildren are added automatically in the listener or during initialisation)
        rootItem.getChildren().add(
            new FileRefTreeItem<>(
                this.rootModel,
                this.graphicsFactory,
                T::children // no parentheses here
            )
        );  
    }    
    
    public T rootModel(){
        return rootModel;
    }
    
    public TreeItem<T> rootItem(){
        return rootItem;
    }
}

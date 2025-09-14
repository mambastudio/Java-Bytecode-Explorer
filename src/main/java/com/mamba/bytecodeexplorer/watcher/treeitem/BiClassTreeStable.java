/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher.treeitem;

import com.mamba.bytecodeexplorer.watcher.FileRef;
import com.mamba.bytecodeexplorer.watcher.FileRefTree;
import java.util.Objects;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author joemw
 */
public class BiClassTreeStable implements FileRefTree<BiClassTreeStable>{
    
    public final FileRef ref;
    public final ObservableList<BiClassTreeStable> children;
    
    public BiClassTreeStable(FileRef ref){
        Objects.requireNonNull(ref);        
        this.ref = ref;
        if(!ref.isDirectory())
            this.children = FXCollections.emptyObservableList();
        else
        {
            this.children = FXCollections.observableArrayList();
            for(FileRef r : ref.children(".class"))
                if(r.isLeaf())
                    this.children.add(new BiClassTreeStable(r));
        }        
    }

    @Override
    public FileRef ref() {
        return ref;
    }

    @Override
    public ObservableList<BiClassTreeStable> children() {
        return children;
    }

    @Override
    public Optional<BiClassTreeStable> findInTree(FileRef x) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.tree.model;

import com.mamba.bytecodeexplorer.core.AbstractFileRefTree;
import com.mamba.bytecodeexplorer.core.Relation;
import com.mamba.bytecodeexplorer.file.FileRef;
import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author joemw
 */
public class ClassRefModel extends AbstractFileRefTree<ClassRefModel>{
    
    public final FileRef ref;
    public final ObservableList<ClassRefModel> children;
    
    public ClassRefModel(FileRef ref, boolean createChildren){
        Objects.requireNonNull(ref);        
        this.ref = ref;      
        
        switch(createChildren){
            case true -> {
                switch(isLeaf()){
                    case true -> this.children = FXCollections.emptyObservableList();
                    case false -> {
                        this.children = FXCollections.observableArrayList();
                        for(FileRef r : ref.children(".class"))
                            if(r.isLeaf())
                                this.children.add(new ClassRefModel(r, false));
                    }
                }
            }
            case false ->{
                switch(isLeaf()){
                    case true -> this.children = FXCollections.emptyObservableList();
                    case false -> this.children = FXCollections.observableArrayList();
                }
            }
        }
    }
    
    public ClassRefModel(){
        this(FileRef.virtualRoot(), false);
    }

    @Override
    public FileRef ref() {
        return ref;
    }
    
    public boolean addChild(ClassRefModel model){        
        if(model.equals(this))
            return false;        
        if(model.isDescendantOf(this)){
            if(children.contains(model))
                return false;
            children.add(model);
            return true;
        }
        return false;
    }

    @Override
    public ObservableList<ClassRefModel> children() {
        return children;
    }
    
    @Override
    public Relation<ClassRefModel> findInTree2(FileRef target) {
        var ctarget = new ClassRefModel(target, false);
        
        if(target == null)
            return Relation.empty();
        
        //check node ref for this is equal to target
        if (ref() != null && ref().equals(target))
            return Relation.asChild(this);
        
        //if not descendant, no point of searching deeper
        if(!target.isDescendantOf(ref()))
            return Relation.empty();
        
        //check if children have any match and return
        if(children().contains(ctarget))
            return new Relation(this, ctarget);
        
        //go next depth
        for (var child : children()) {
            var match = child.findInTree2(target);
            if (match.isPresent()) return match;
        }

        return Relation.empty();
    }   
}

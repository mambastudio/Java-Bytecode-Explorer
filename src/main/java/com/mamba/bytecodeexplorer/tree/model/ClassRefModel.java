/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.tree.model;

import com.mamba.bytecodeexplorer.core.AbstractFileRefTree;
import com.mamba.bytecodeexplorer.file.type.FileRef;
import com.mamba.bytecodeexplorer.file.type.RealFile;
import com.mamba.bytecodeexplorer.file.type.VirtualFile;
import java.io.IO;
import java.util.ArrayList;
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
    private boolean classChildrenIntended = false;
    
    public ClassRefModel(FileRef ref, boolean createChildren){
        Objects.requireNonNull(ref);        
        this.ref = ref;      
        
        switch(createChildren){
            case true -> {           
                this.classChildrenIntended = true;
                this.children = FXCollections.observableArrayList();     
                if(ref instanceof RealFile f)
                    for(var r : f.children(".class"))
                        if(r.isLeaf())
                            this.children.add(new ClassRefModel(r, false));                    
            }
            case false ->this.children = FXCollections.observableArrayList();            
        }
    }
    
    public ClassRefModel(){
        this(new VirtualFile(), false);
    }

    @Override
    public FileRef ref() {
        return ref;
    }
        
    public boolean addChild(ClassRefModel model){        
        if(!classChildrenIntended() && model.isClassFile())
            return false;
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
    
    public boolean isClassFile(){
        return ref() instanceof RealFile f && f.isLeaf();
    }
    
    public boolean classChildrenIntended(){
        return classChildrenIntended;
    }
    
    @Override
    public ObservableList<ClassRefModel> children() {
        return children;
    }
}

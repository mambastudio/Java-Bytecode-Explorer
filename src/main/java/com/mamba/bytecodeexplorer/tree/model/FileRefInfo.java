/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.bytecodeexplorer.tree.model;

import com.mamba.bytecodeexplorer.tree.AbstractFileRefTree;
import com.mamba.bytecodeexplorer.file.FileExtensions;
import com.mamba.bytecodeexplorer.file.FileRef;
import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author user
 */
public class FileRefInfo extends AbstractFileRefTree<FileRefInfo>{
    private final FileRef fileRef;
    private final ObservableList<FileRefInfo> children = FXCollections.observableArrayList();
    private final FileExtensions extensions;
    private String name;
    
    public FileRefInfo(FileRef ref, String... extensions){
        Objects.requireNonNull(ref);
        this.extensions = switch(extensions != null){
                                case true -> new FileExtensions(extensions);
                                case false -> new FileExtensions(".class");
                            };
        this.fileRef = ref;
        this.name = fileRef.name();
    }
    
    public FileRef getFileRef(){
        return fileRef;
    }
     
    public void reloadSystemChildren(){
        if(fileRef.exists() && fileRef.isDirectory()){
            FileRef[] childrenFileRef = fileRef.children(extensions);
            children.clear();
            for(FileRef ref : childrenFileRef){
                if(ref.isLeaf())
                    children.add(new FileRefInfo(ref));
            }
        }
    }
    
    public void setName(FileRef ref){
        Objects.requireNonNull(ref);        
        switch(ref){
            case FileRef r when r.isAncestorOf(fileRef) -> name = fileRef.name();
            case FileRef r when r.isDescendantOf(fileRef) -> throw new UnsupportedOperationException("Changing file name requires the name to be from a file that is an ancestor");
            default -> throw new UnsupportedOperationException("No lineage");
        }        
    }
    
    public String name(){
        return name;
    }
    
    public String actualName(){
        return fileRef.name();
    }
    
    public boolean isActualName(){
        return name.equals(fileRef.name());
    }
        
    @Override
    public String toString(){
        return name;
    }

    @Override
    public ObservableList<FileRefInfo> children() {
        return children;
    }
    
    public void clearChildren(){
        children.clear();
    }
    
    public void addChildren(FileRefInfo fileRefInfo){
        children.add(fileRefInfo);
    }

    @Override
    public FileRef ref() {
        return fileRef;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.bytecodeexplorer.dialog;

import com.mamba.bytecodeexplorer.watcher.FileExtensions;
import com.mamba.bytecodeexplorer.watcher.FileRef;

/**
 *
 * @author user
 */
public record FolderTreePair(FileRef ancestorFolder, FileRef parentFolder, FileExtensions extensions) {
    public FolderTreePair(FileRef ancestorFolder, FileRef parentFolder){
        this(ancestorFolder, parentFolder, new FileExtensions(".class"));
    }
    
}

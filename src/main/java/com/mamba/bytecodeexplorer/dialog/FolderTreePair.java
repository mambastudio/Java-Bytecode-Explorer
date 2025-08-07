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
public record FolderTreePair(FileRef parentFolder, FileRef descendantFolder, FileExtensions extensions) {
    public FolderTreePair(FileRef parentFolder, FileRef descendantFolder){
        this(parentFolder, descendantFolder, new FileExtensions(".class"));
    }
}

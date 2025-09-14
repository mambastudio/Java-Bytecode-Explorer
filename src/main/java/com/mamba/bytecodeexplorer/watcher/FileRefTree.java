/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher;

import com.mamba.bytecodeexplorer.Tree;

/**
 *
 * @author user
 * @param <Y>
 */
public interface FileRefTree<Y extends FileRefTree<Y>> extends Tree<FileRef, Y>{
    
}

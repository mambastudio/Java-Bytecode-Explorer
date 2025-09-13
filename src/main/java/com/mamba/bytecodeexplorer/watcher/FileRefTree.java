/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher;

import java.util.List;
import javafx.collections.ObservableList;

/**
 *
 * @author user
 */
public interface FileRefTree {
    FileRef ref();
    ObservableList<? extends FileRefTree> children();
}

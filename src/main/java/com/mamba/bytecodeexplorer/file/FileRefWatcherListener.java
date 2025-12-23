/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.file.type.FileRef;

/**
 *
 * @author user
 */
public interface FileRefWatcherListener {
    public void onCreate(FileRef parent, FileRef child);
    public void onModify(FileRef parent, FileRef child);
    public void onDelete(FileRef parent, FileRef child);
    public void overflow(FileRef root);
}
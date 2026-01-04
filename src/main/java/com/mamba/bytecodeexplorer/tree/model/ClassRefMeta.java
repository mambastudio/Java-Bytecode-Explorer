/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.tree.model;

import com.mamba.bytecodeexplorer.file.FileRefWatcher.FileRefMeta;
import com.mamba.bytecodeexplorer.file.type.FileRef;

/**
 *
 * @author joemw
 */
public record ClassRefMeta(
    FileRef parent, FileRef value, boolean isOpen, boolean classChildrenIntended) implements FileRefMeta {}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.file.FileRefWatcher.FileRefMeta;
import java.nio.file.Path;

/**
 *
 * @author joemw
 */
@FunctionalInterface
public interface MetaProvider {
    FileRefMeta metaFor(Path path);
}

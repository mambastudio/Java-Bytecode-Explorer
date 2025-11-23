/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.core;

import java.nio.file.Path;
import java.time.Instant;

/**
 *
 * @author joemw
 */
public interface FileRefMeta {
    /** Path to the file or directory this metadata represents.
     * @return  */
    public  Path path();

    /** Last known modification time (if applicable).
     * @return  */
    public Instant lastModified();

    /** Whether this entry exists physically on disk.
     * @return  */
    public boolean exists();
}

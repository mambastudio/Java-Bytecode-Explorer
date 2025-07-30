/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.bytecodeexplorer.watcher;

/**
 *
 * @author user
 */
public record FileExtensions(String... extensions) {
    public FileExtensions{
        if(extensions == null)
            extensions = new String[0];
    }
    
    public FileExtensions(){
        this(new String[0]);
    }
    public boolean hasExtensions() {
            return extensions.length > 0;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.file.type;

/**
 *
 * @author joemw
 */
public record VirtualFile(String name) implements FileRef{    
    public VirtualFile{
        if(name == null){name = "virtual";}
    }
    
    public VirtualFile(){this("virtual");}

    @Override
    public boolean isVirtual() {
        return true;
    }    
}

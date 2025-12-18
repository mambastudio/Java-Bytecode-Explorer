/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.file;

import java.io.IO;

/**
 *
 * @author joemw
 */
public class TestFile {
    void main(){
        FileRef2 f = new RealFile("C:\\Users\\joemw\\OneDrive\\Documents\\GitHub\\mambaui-fx");
        
        switch(f){
            case VirtualFile _ -> IO.println("virtual file");
            case RealFile r -> IO.println(r);
        }
        
    }
}

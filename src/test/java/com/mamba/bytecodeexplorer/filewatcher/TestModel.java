/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.filewatcher;

import com.mamba.bytecodeexplorer.file.FileRef;
import com.mamba.bytecodeexplorer.tree.model.ClassRefModel;
import java.io.IO;

/**
 *
 * @author joemw
 */
public class TestModel {
    void main(){
        var root = new ClassRefModel(new FileRef("C:\\Users\\joemw\\OneDrive\\Documents\\GitHub\\mambaui-fx"), false);
        var folder = new ClassRefModel(new FileRef("C:\\Users\\joemw\\OneDrive\\Documents\\GitHub\\mambaui-fx\\target\\classes\\com\\mamba\\mambaui\\base"), true);
        var classRef = new FileRef("C:\\Users\\joemw\\OneDrive\\Documents\\GitHub\\mambaui-fx\\target\\classes\\com\\mamba\\mambaui\\base\\RectLayout.class");
        
        root.addChild(folder);
        
        var virtualRoot = new ClassRefModel();
        virtualRoot.addChild(root);
        
        IO.println(virtualRoot.treeString());
        
        var result = virtualRoot.findInTree(classRef);
        new FileRef("C:\\Users\\joemw\\OneDrive\\Documents\\GitHub\\mambaui-fx\\target\\classes\\com\\mamba\\mambaui\\base\\RectLayout.class").path().toFile();
        IO.println(result);
    }
}

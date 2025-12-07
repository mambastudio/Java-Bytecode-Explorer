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
 * @author user
 */
public class ModelSearch {
    void main(){
        test2();
    }
    
    public void test1(){
        var root = new ClassRefModel();
        var model = new ClassRefModel(new FileRef("C:\\Users\\user\\Documents\\NetBeansProjects\\Bitmap"), false);
        model.addChild(new ClassRefModel(new FileRef("C:\\Users\\user\\Documents\\NetBeansProjects\\Bitmap\\build\\modules\\bitmap\\bitmap"), true));
        root.addChild(model);
        
        var f = new FileRef("C:\\Users\\user\\Documents\\NetBeansProjects\\Bitmap\\build\\modules\\bitmap\\bitmap\\XYZ.class");
        
        IO.println(root.findInTree2(f).isPresent());
    }
    
    public void test2(){
        var parent = new FileRef("C:\\Users\\user\\Documents\\NetBeansProjects\\Bitmap\\build\\modules\\bitmap\\bitmap");
        var child = new FileRef("C:\\Users\\user\\Documents\\NetBeansProjects\\Bitmap\\build\\modules\\bitmap\\bitmap\\XYZ.class");
        
        IO.println(parent.isAncestorOf(child));
        IO.println(child.path().startsWith(parent.path()));
    }
}

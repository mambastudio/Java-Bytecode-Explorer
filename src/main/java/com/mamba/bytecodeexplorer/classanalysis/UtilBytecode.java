/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.classanalysis;

import java.lang.classfile.AccessFlags;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author joemw
 */
public class UtilBytecode {
    public static List<String> methodModifiers(AccessFlags f) {
        List<String> mods = new ArrayList<>();

        if (f.has(AccessFlag.PUBLIC))    mods.add("public");
        if (f.has(AccessFlag.PROTECTED)) mods.add("protected");
        if (f.has(AccessFlag.PRIVATE))   mods.add("private");

        if (f.has(AccessFlag.ABSTRACT))  mods.add("abstract");
        if (f.has(AccessFlag.STATIC))    mods.add("static");
        if (f.has(AccessFlag.FINAL))     mods.add("final");
        if (f.has(AccessFlag.SYNCHRONIZED)) mods.add("synchronized");
        if (f.has(AccessFlag.NATIVE))    mods.add("native");
        if (f.has(AccessFlag.STRICT))    mods.add("strictfp");

        return mods;
    }
    
    public static String toBinaryClassName(String className){
        Objects.requireNonNull(className);
        int index = className.lastIndexOf('/');
        return index >= 0 ? className.substring(index + 1) : className;
    }
    
    public static String toSimpleName(String className){
        Objects.requireNonNull(className);
        int index = className.lastIndexOf('$');
        return index >= 0 ? className.substring(index + 1) : className;
    }
}
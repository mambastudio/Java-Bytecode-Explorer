/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import java.lang.classfile.AccessFlags;
import java.lang.classfile.Attributes;
import java.lang.classfile.MethodModel;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;

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
    
    
}
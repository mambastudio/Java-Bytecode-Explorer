/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.classanalysis;

import java.lang.classfile.ClassModel;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.InvokeDynamicEntry;
import java.lang.classfile.constantpool.MethodRefEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.lang.classfile.constantpool.StringEntry;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joemw
 */
public sealed interface ConstantPoolInfo {
    
    public enum Kind {
        CLASS, METHODREF, FIELDREF, STRING, INVOKEDYNAMIC, OTHER
    }
    
    public int index();
    public Kind kind();
    public String summary();
    
    public record ClassInfo(
            int index, 
            String internalName
    ) implements ConstantPoolInfo {
        
        @Override public Kind kind() {
            return Kind.CLASS;
        }
        @Override public String summary() {
            return internalName;
        }
    }
    
    public record MethodInfo(
            int index,
            String owner,
            String name,
            String descriptor
    ) implements ConstantPoolInfo {

        @Override public Kind kind() {
            return Kind.METHODREF;
        }

        @Override public String summary() {
            return owner + "." + name + descriptor;
        }
    }
    
    public record FieldInfo(
        int index,
        String owner,
        String name,
        String descriptor
    ) implements ConstantPoolInfo {

        @Override public Kind kind() {
            return Kind.FIELDREF;
        }

        @Override public String summary() {
            return owner + "." + name + " : " + descriptor;
        }
    }
    
    public record StringLitInfo(
        int index,
        String value
    ) implements ConstantPoolInfo {

        @Override public Kind kind() {
            return Kind.STRING;
        }

        @Override public String summary() {
            return "\"" + value + "\"";
        }
    }

    public record InvokeDynamic(
        int index,
        String name,
        String descriptor,
        int bootstrapIndex
    ) implements ConstantPoolInfo {

        @Override public Kind kind() {
            return Kind.INVOKEDYNAMIC;
        }

        @Override public String summary() {
            return name + descriptor;
        }
    }

    //Fallback
    public record Other(
        int index,
        String summary
    ) implements ConstantPoolInfo {
        
        @Override public Kind kind() {
            return Kind.OTHER;
        }
    }
    
    public static List<ConstantPoolInfo> project(ClassModel cm) {
        var rows = new ArrayList<ConstantPoolInfo>();

        for (PoolEntry e : cm.constantPool()) {
            rows.add(projectEntry(e.index(), e));
        }

        return rows;
    }

    private static ConstantPoolInfo projectEntry(            
            int index,
            PoolEntry e
    ) {
        return switch (e) {

            case ClassEntry c ->
                new ConstantPoolInfo.ClassInfo(
                    index,
                    c.name().stringValue()
                );

            case MethodRefEntry m -> {
                var owner = m.owner().name().stringValue();
                var nt = m.nameAndType();
                yield new ConstantPoolInfo.MethodInfo(
                    index,
                    owner,
                    nt.name().stringValue(),
                    nt.type().stringValue()
                );
            }

            case FieldRefEntry f -> {
                var owner = f.owner().name().stringValue();
                var nt = f.nameAndType();
                yield new ConstantPoolInfo.FieldInfo(
                    index,
                    owner,
                    nt.name().stringValue(),
                    nt.type().stringValue()
                );
            }

            case StringEntry s ->
                new ConstantPoolInfo.StringLitInfo(
                    index,
                    s.stringValue()
                );

            case InvokeDynamicEntry indy -> {
                var nt = indy.nameAndType();
                yield new ConstantPoolInfo.InvokeDynamic(
                    index,
                    nt.name().stringValue(),
                    nt.type().stringValue(),
                    indy.bootstrapMethodIndex()
                );
            }

            // You intentionally collapse low-level entries
            default ->
                new ConstantPoolInfo.Other(
                    index,
                    e.getClass().getSimpleName()
                );
        };
    }
    
    public static String format(ConstantPoolInfo info) {
        return switch (info) {

            case ConstantPoolInfo.MethodInfo m ->
                String.format(
                    "#%-3d = %-12s %-12s // %s",
                    m.index(),
                    "Methodref",
                    "", // raw refs omitted for now
                    m.summary()
                );

            case ConstantPoolInfo.FieldInfo f ->
                String.format(
                    "#%-3d = %-12s %-12s // %s",
                    f.index(),
                    "Fieldref",
                    "",
                    f.summary()
                );

            case ConstantPoolInfo.ClassInfo c ->
                String.format(
                    "#%-3d = %-12s %-12s // %s",
                    c.index(),
                    "Class",
                    "",
                    c.summary()
                );

            case ConstantPoolInfo.StringLitInfo s ->
                String.format(
                    "#%-3d = %-12s %-12s // %s",
                    s.index(),
                    "String",
                    "",
                    s.summary()
                );

            case ConstantPoolInfo.InvokeDynamic indy ->
                String.format(
                    "#%-3d = %-12s %-12s // %s",
                    indy.index(),
                    "InvokeDynamic",
                    "",
                    indy.summary()
                );

            case ConstantPoolInfo.Other o ->
                String.format(
                    "#%-3d = %-12s",
                    o.index(),
                    o.summary()
                );
        };
    }

}

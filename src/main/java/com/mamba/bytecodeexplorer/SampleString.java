/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer;

/**
 *
 * @author user
 */
public class SampleString {
    public static String str = """
                               aload_0         -> slot 0
                               invokespecial   -> owner = Object, method = <init>, parameters = (), return type = ()V
                               aload_0         -> slot 0
                               ldc2_w          -> 100
                               iconst_1
                               anewarray       -> MemoryLayout[]
                               dup
                               iconst_0
                               getstatic       -> owner = ValueLayout, field = JAVA_INT, type = ValueLayout$OfInt;
                               ldc             -> value
                               invokeinterface -> owner = ValueLayout$OfInt, method = withName, parameters = (String), return type = ValueLayout$OfInt;
                               aastore
                               invokestatic    -> owner = MemoryLayout, method = structLayout, parameters = (MemoryLayout[]), return type = StructLayout;
                               ldc             -> Int32
                               invokeinterface -> owner = StructLayout, method = withName, parameters = (String), return type = StructLayout;
                               invokestatic    -> owner = MemoryLayout, method = sequenceLayout, parameters = (long, MemoryLayout), return type = SequenceLayout;
                               ldc             -> int32
                               invokeinterface -> owner = SequenceLayout, method = withName, parameters = (String), return type = SequenceLayout;
                               putfield        -> owner = Test, field = layout, type = MemoryLayout;
                               aload_0         -> slot 0
                               aload_0         -> slot 0
                               getfield        -> owner = Test, field = layout, type = MemoryLayout;
                               iconst_2
                               anewarray       -> MemoryLayout$PathElement[]
                               dup
                               iconst_0
                               invokestatic    -> owner = MemoryLayout$PathElement, method = sequenceElement, parameters = (), return type = MemoryLayout$PathElement;
                               aastore
                               dup
                               iconst_1
                               ldc             -> value
                               invokestatic    -> owner = MemoryLayout$PathElement, method = groupElement, parameters = (String), return type = MemoryLayout$PathElement;
                               aastore
                               invokeinterface -> owner = MemoryLayout, method = varHandle, parameters = (MemoryLayout$PathElement[]), return type = VarHandle;
                               putfield        -> owner = Test, field = valueInt32Int32SequenceLayoutImplHandle, type = VarHandle;
                               return""";
}

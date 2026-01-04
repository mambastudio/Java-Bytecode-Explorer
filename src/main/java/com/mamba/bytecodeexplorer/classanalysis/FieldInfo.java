/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.bytecodeexplorer.classanalysis;

import java.lang.classfile.instruction.FieldInstruction;
import java.lang.constant.ClassDesc;

/**
 *
 * @author joemw
 */
public record FieldInfo(FieldInstruction fis) {
    @Override
    public String toString(){
        var opcode = fis.opcode().name().toLowerCase();
        var ref = fis.field();
        var owner  = ref.owner().name().stringValue().replace('/', '.');
        var name   = ref.name().stringValue();
        var type   = ClassDesc.ofDescriptor(ref.type().stringValue()).displayName();
        
        return String.format("%s.%s : %s",
            opcode, owner, name, type);
    }
}

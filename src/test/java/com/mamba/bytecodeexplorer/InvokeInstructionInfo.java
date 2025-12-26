/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import java.lang.classfile.instruction.InvokeInstruction;

/**
 *
 * @author joemw
 */
public record InvokeInstructionInfo(InvokeInstruction is) {
    @Override
    public String toString(){
        return is.opcode().toString();
    }
}

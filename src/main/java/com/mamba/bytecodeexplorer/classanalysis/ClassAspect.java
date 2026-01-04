/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mamba.bytecodeexplorer.classanalysis;

/**
 *
 * @author joemw
 */
public enum ClassAspect {
    METHODS("Methods"),
    FIELDS("Fields"),
    CONSTANT_POOL("Constant Pool");

    private final String title;

    ClassAspect(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}

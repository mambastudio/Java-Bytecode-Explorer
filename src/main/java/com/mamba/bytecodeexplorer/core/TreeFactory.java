/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.core;

/**
 *
 * @author joemw
 */
@FunctionalInterface
public interface TreeFactory<X, Y> {
    Y create(X x, boolean children);
}

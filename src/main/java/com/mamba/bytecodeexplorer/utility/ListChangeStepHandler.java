/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.utility;

/**
 *
 * @author user
 * @param <T>
 */
@FunctionalInterface
public interface ListChangeStepHandler<T> {
    void handle(ListChangeCase<T> changeStep);
}

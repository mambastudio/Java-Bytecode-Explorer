/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import javafx.collections.ObservableList;

/**
 *
 * @author user
 * @param <T>
 * @param <S>
 */
public interface RecursiveTreeModel<T, S extends RecursiveTreeModel<T, S>> {
    public ObservableList<S> children();
}

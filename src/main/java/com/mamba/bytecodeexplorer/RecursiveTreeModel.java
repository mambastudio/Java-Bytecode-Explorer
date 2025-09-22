/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer;

import javafx.collections.ObservableList;

/**
 *
 * @author user
 * @param <S>
 */
public interface RecursiveTreeModel<S extends RecursiveTreeModel<S>> {
    public ObservableList<S> children();
}

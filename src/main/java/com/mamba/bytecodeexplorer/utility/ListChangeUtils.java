/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.utility;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;

/**
 *
 * @author user
 */
public class ListChangeUtils {
    public static <T> ListChangeListener<T> asListener(ListChangeStepHandler<T> handler) {
        return change -> {
            while (change.next()) {
                @SuppressWarnings("unchecked")
                Change<? extends T> typed = (Change<? extends T>) change;
                ListChangeCase<T> classified = ListChangeCase.classify(typed);
                handler.handle(classified);
            }
        };
    }

    private ListChangeUtils() {}
}

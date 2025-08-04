/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.utility;

import javafx.collections.ListChangeListener.Change;

/**
 *
 * @author user
 * @param <T>
 */
public interface ListChangeCase<T> {
    public enum Kind {ADDED, REMOVED}
    
    public record PermutationCase<T>(Change<? extends T> change) implements ListChangeCase<T> {}
    public record UpdateCase<T>(Change<? extends T> change) implements ListChangeCase<T> {}
    public record AddRemoveCase<T>(Change<? extends T> change, Kind kind) implements ListChangeCase<T> {}
    public record ReplacedCase<T>(Change<? extends T> change) implements ListChangeCase<T> {}

    public static <T> ListChangeCase<T> classify(Change<? extends T> c) {
        if (c.wasPermutated()) return new PermutationCase<>(c);
        if (c.wasUpdated())    return new UpdateCase<>(c);
        if (c.wasAdded() && !c.wasRemoved())  return new AddRemoveCase<>(c, Kind.ADDED);
        if (c.wasRemoved() && !c.wasAdded())  return new AddRemoveCase<>(c, Kind.REMOVED);
        if (c.wasAdded() && c.wasRemoved()) return new ReplacedCase<>(c);
        
        throw new IllegalStateException("Unknown change: " + c);
    }
}

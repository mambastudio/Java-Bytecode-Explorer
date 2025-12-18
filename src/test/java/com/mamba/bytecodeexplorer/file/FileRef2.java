/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.bytecodeexplorer.file;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author joemw
 */
public sealed interface FileRef2 permits RealFile, VirtualFile{
    public enum ExploreType { FILE_EXPLORE, FOLDER_EXPLORE, FILE_OR_FOLDER_EXPLORE }
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");    
    public boolean isVirtual();
    
    default boolean isAncestorOf(FileRef2 child) {        
        Objects.requireNonNull(child, "method parameter child must not be null");
        return switch (this) {            
            case VirtualFile _ -> true; // Virtual root is ancestor of everything except null
            case RealFile parent ->
                switch (child) {                    
                    case VirtualFile _ -> false; // Real file can never be ancestor of virtual root
                    case RealFile r -> !parent.equals(r) && r.path().startsWith(parent.path());
                };
        };
    }

    default boolean isDescendantOf(FileRef2 parent) {
        return parent != null && parent.isAncestorOf(this);
    }
    
    default String sizeReadable() {
        if(!(this instanceof RealFile f))
            return "-";
        
        long bytes = f.file().length();
        return switch ((int) (Math.log10(bytes == 0 ? 1 : bytes) / 3)) {
            case 0 -> String.format("%1$3.3g  B", (float) bytes);
            case 1 -> String.format("%1$3.3g KB", bytes / 1024f);
            case 2 -> String.format("%1$3.3g MB", bytes / (1024f * 1024f));
            case 3 -> String.format("%1$3.3g GB", bytes / (1024f * 1024f * 1024f));
            case 4 -> String.format("%1$3.3g TB", bytes / (1024f * 1024f * 1024f * 1024f));
            case 5 -> String.format("%1$3.3g PB", bytes / (1024f * 1024f * 1024f * 1024f * 1024f));
            case 6 -> String.format("%1$3.3g EB", bytes / (1024f * 1024f * 1024f * 1024f * 1024f * 1024f));
            default -> "Too large";
        };
    }
    
    default boolean hasParent() {
        return switch(this){
            case VirtualFile _ -> false;
            case RealFile r -> r.path().getParent() != null;
        };
    }
    
    default Optional<RealFile> parent() {
        return switch(this){
            case VirtualFile _ -> Optional.empty();
            case RealFile r -> Optional.of(new RealFile(r.path().getParent()));
        };
    }    
}

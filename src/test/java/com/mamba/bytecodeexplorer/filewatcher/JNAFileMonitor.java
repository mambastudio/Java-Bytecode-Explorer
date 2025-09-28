/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.filewatcher;

import com.mamba.bytecodeexplorer.file.FileWatcher;
import com.mamba.bytecodeexplorer.file.FileWatcherRegistry;
import com.mamba.bytecodeexplorer.file.FileWatcher.FileEvent;
import java.io.File;
import java.io.IO;

/**
 *
 * @author user
 */
public class JNAFileMonitor {
    
    void main() throws Exception {
        FileWatcher watcher = FileWatcherRegistry.getOrCreate(
        new File("C:\\Users\\joemw\\OneDrive\\Desktop\\Josto2"),
        FileWatcher.Mode.RECURSIVE);

        watcher.addEventHandler(e -> {
            if(e instanceof FileEvent(FileWatcher.FileEventEnum type, File file))
                switch (type) {                
                    case FILE_CREATED -> IO.println("Created: " + file);
                    case FILE_DELETED -> IO.println("Deleted: " + file);
                    case FILE_MODIFIED -> IO.println("Modified: " + file);
                    case FILE_RENAMED -> IO.println("Renamed: " + file);
                    default -> IO.println("Other event: " + type + " on " + file);
                }
            });

        Thread.sleep(60_000); // keep running for 1 minute

        FileWatcherRegistry.stopAll();
    }
}

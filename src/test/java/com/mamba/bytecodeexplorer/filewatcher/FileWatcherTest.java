/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.filewatcher;

import com.mamba.bytecodeexplorer.file.FileRef;
import com.mamba.bytecodeexplorer.file.FileRefWatcher;
import com.mamba.bytecodeexplorer.file.FileRefWatcherListener;
import java.io.IO;

/**
 *
 * @author joemw
 */
public class FileWatcherTest {
    void main() throws InterruptedException{
        FileRefWatcher watcher = new FileRefWatcher();
        watcher.setMonitor(new FileRef("C:\\Users\\joemw\\OneDrive\\Desktop\\Josto2\\Josto"));
        watcher.registerListener(new FileRefWatcherListener(){
            @Override
            public void onCreate(FileRef parent, FileRef child) {
                IO.println("asdfasdf");
            }

            @Override
            public void onModify(FileRef parent, FileRef child) {
            }

            @Override
            public void onDelete(FileRef parent, FileRef child) {
            }

            @Override
            public void overflow(FileRef root) {
                IO.println(root.name() +" deleted");
            }
            
        });
        watcher.processEvents();
        
        Thread.sleep(3600_000); // keep running for 1 minute
    }
}

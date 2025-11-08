/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.filewatcher;

import com.mamba.bytecodeexplorer.file.FileRef;
import com.mamba.bytecodeexplorer.file.FileRefWatcher;
import com.mamba.bytecodeexplorer.file.FileRefWatcher2;
import com.mamba.bytecodeexplorer.file.FileRefWatcherListener;
import java.io.IO;
import java.nio.file.Paths;

/**
 *
 * @author joemw
 */
public class FileWatcherTest {
    void main() throws InterruptedException{
        test2();
    }
    
    void test1() throws InterruptedException{
        FileRefWatcher watcher = new FileRefWatcher();
        watcher.setMonitor(new FileRef("C:\\Users\\user\\Desktop\\Kubafu"));
        watcher.registerListener(new FileRefWatcherListener(){
            @Override
            public void onCreate(FileRef parent, FileRef child) {
                IO.println("created: " +child.name());
            }

            @Override
            public void onModify(FileRef parent, FileRef child) {
                IO.println("modify: " +child.name());
            }

            @Override
            public void onDelete(FileRef parent, FileRef child) {
                IO.println("deleted: " +child.name());
            }

            @Override
            public void overflow(FileRef root) {
                IO.println(root.name() +" deleted: ");
            }
            
        });
        watcher.processEvents();
        
        Thread.sleep(3600_000); // keep running for 1 minute
    }
    
    void test2() throws InterruptedException{
        FileRefWatcher2 watcher = FileRefWatcher2.getInstance();
        watcher.setEventDelayedTo(200);
        watcher.watch(Paths.get("C:\\Users\\user\\Desktop\\Kubafu"), e->{
            IO.println(e+ " ");
            
        });
        Thread.sleep(3600_000); // keep running for 1 minute
    }
}

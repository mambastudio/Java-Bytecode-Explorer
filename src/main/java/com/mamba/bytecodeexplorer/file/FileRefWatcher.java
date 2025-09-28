/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.tree.FileRefTree;
import java.io.IO;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class FileRefWatcher {
    private WatchService watcher;
    private final Map<WatchKey, FileRef> keys = new HashMap<>();
    private Optional<FileRefWatcherListener> listener = Optional.empty();
    private Optional<DelayedDirectoryScanner> scanner = Optional.empty();
    
    private volatile boolean running = true;
    
    private final long delayMilliseconds;
    
    public FileRefWatcher(){
        this(0);
    }
    
    public FileRefWatcher(long delayMilliseconds){
        this.delayMilliseconds = delayMilliseconds;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!running) return;

            running = false;
            if (watcher != null) try {
                watcher.close();
                scanner.ifPresent(s -> s.shutdown());
            } catch (IOException ex) {
                Logger.getLogger(FileRefWatcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }));
    }
    
    public final void setMonitor(FileRef root){ 
        try {
            if(this.watcher != null) {
                this.watcher.close(); // safely close any existing watcher               
            }
            
            this.watcher = FileSystems.getDefault().newWatchService();
            //close existing key if any
            keys.keySet().forEach(key -> {
                key.cancel();
            });
            keys.clear();
            //register new keys
            this.walkAndRegisterDirectories(root);
        } catch (IOException ex) {
            Logger.getLogger(FileRefWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public final void setMonitor(FileRefTree root){ 
        try {
            if(this.watcher != null) {
                this.watcher.close(); // safely close any existing watcher               
            }
            
            this.watcher = FileSystems.getDefault().newWatchService();
            //close existing key if any
            keys.keySet().forEach(key -> {
                key.cancel();
            });
            keys.clear();
            //register new keys
            this.walkAndRegisterDirectories(root);
        } catch (IOException ex) {
            Logger.getLogger(FileRefWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void registerDirectory(FileRef dir) throws IOException{       
        WatchKey key = dir.path().register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
        keys.put(key, dir);
    }
    
    private void walkAndRegisterDirectories(FileRef rootDirectory){
        if(rootDirectory.isLeaf())
            return;
        
        try {
            // register directory and sub-directories
            Files.walkFileTree(rootDirectory.path(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    registerDirectory(new FileRef(dir));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(FileRefWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private<T extends FileRefTree<T>> void walkAndRegisterDirectories(T rootTree) { 
        FileRef ref = rootTree.ref();
        if (ref == null || ref.isLeaf()) {
            return;
        }

        try {
            // Register this directory
            if (ref.isDirectory()) {
                registerDirectory(ref);
            }

            // Recurse into logical children
            for (FileRefTree child : rootTree.children()) {
                walkAndRegisterDirectories(child);
            }
        } catch (IOException ex) {
            Logger.getLogger(FileRefWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public void processEvents(){             
        Thread thread = new Thread(()->{
            while(running){
                // wait for key to be signalled
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException | ClosedWatchServiceException e) {
                    break;
                }
                
                Path dir = keys.get(key).path();
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    continue;
                }
                
                key.pollEvents().forEach((event) -> {                    
                    WatchEvent.Kind kind = event.kind();
                    
                    // Context for directory entry event is the file name of entry                   
                    Path name = ((WatchEvent<Path>)event).context();
                    Path child = dir.resolve(name);                   
                    FileRef childFile = new FileRef(child);
                                       
                    // If a directory is created and we're watching recursively, register it and its subdirectories.
                    // In some cases (e.g., build tools), files and folders may be batch-created very quickly,
                    // and we might miss their creation events if the directory wasn't registered in time.
                    // To handle this, we trigger a delayed manual scan to simulate missed creation events.
                    // Note: The listener callback is invoked using safelyRun() to guard against exceptions (e.g., null access),
                    // ensuring the event thread remains alive and responsive.
                    if (kind == ENTRY_CREATE) {                        
                        if (Files.isDirectory(child)){
                            walkAndRegisterDirectories(new FileRef(child));                            
                            
                            // If a directory is created, schedule a delayed scan to capture any files or subdirectories
                            // that might have been created before the watcher was registered. 
                            // Note: This may trigger duplicate onCreate events for the same files 
                            // (one from WatchService, one from the delayed scan), 
                            // so the listener implementation should handle de-duplication.
                            if(delayMilliseconds > 0)                                
                                scanner.ifPresent(s -> s.scanLater(childFile, delayMilliseconds)); //delayed read
                        }       
                        
                        safelyRun(() -> listener.ifPresent(cb -> cb.onCreate(keys.get(key), childFile)), "create");                          
                    }                    
                    if(kind == ENTRY_MODIFY) {
                        safelyRun(() -> listener.ifPresent(cb -> cb.onModify(keys.get(key), childFile)), "modify");                        
                    }
                    if(kind == ENTRY_DELETE) {
                        safelyRun(() -> listener.ifPresent(cb -> cb.onDelete(keys.get(key), childFile)), "delete");                   
                    }
                    if(kind == OVERFLOW){
                        IO.println("Kubafu");
                        FileRef rootRef = keys.get(key);
                        safelyRun(() -> listener.ifPresent(cb -> {
                            cb.overflow(rootRef);    // signal overflow
                            if (delayMilliseconds > 0) {
                                scanner.ifPresent(s -> s.scanLater(rootRef, delayMilliseconds));
                            }
                        }), "overflow");
                    }
                });
                
                // reset key and remove from set if directory no longer accessible (like when it's deleted)
                boolean valid = key.reset();
                if (!valid) {
                    IO.println("kaudasdf");
                    keys.remove(key);

                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        running = false;
                        break;
                    }
                }
            }            
        }, "FileRefWatcher-Thread");
        thread.setDaemon(true);
        thread.start();
    }
    
    private void safelyRun(Runnable action, String label) {
        try {
            action.run();
        } catch (Exception e) {
            Logger.getLogger(FileRefWatcher.class.getName()).log(Level.WARNING, "Error in " + label + " callback", e);
        }
    }
    
    public void stopWatching() throws IOException {
        running = false;
        watcher.close(); // also unblocks take()
        scanner.ifPresent(s -> s.shutdown());
    }
    
    public void registerListener(FileRefWatcherListener listener){
        this.listener = Optional.of(listener);
        scanner.ifPresent(s -> s.shutdown());
        scanner = Optional.of(new DelayedDirectoryScanner(listener));
    }    
}
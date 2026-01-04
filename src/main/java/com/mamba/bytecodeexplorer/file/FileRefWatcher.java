/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.core.AbstractFileRefTree;
import com.mamba.bytecodeexplorer.file.FileRefWatcher.FileEventListener.FileEvent;
import com.mamba.bytecodeexplorer.file.type.RealFile;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jmburu
 */
public class FileRefWatcher {
    
    // ============================================================
    // Internal helper types (kept inside the same file for portability)
    // ============================================================
    
    public interface FileRefMeta{}

    /**
     * Mutable state for an actively watched directory.
     * Holds the WatchKey and the list of active listeners.
     */
    private static final class DirState {
        WatchKey key;
        final List<FileEventListener> listeners = new CopyOnWriteArrayList<>();
        
        DirState(WatchKey key) {
            this.key = key;
        }
    }
    
    /**
     * Immutable snapshot for a directory that disappeared.
     * Stores the timestamp of invalidation and the listeners
     * that must be restored if the directory reappears.
     */
    private record InvalidationInfo(long timestamp, List<FileEventListener> retainedListeners) {}
    
    private final Map<Path, FileRefMeta> metaMap = new ConcurrentHashMap<>();
    
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true); // This makes the thread non-blocking for JVM shutdown
                t.setName("DelayedSchedule-Thread");
                return t;
            });
      
    private final WatchService watcher;
    // Active directories being watched: dir → DirState(key + listeners)
    private final Map<Path, DirState> states = new ConcurrentHashMap<>();
    // Directories whose watcher key failed (deleted/moved): dir → InvalidationInfo(timestamp + retainedListeners)
    private final Map<Path, InvalidationInfo> invalidations = new ConcurrentHashMap<>();
    private Thread thread;
    
    private long delayTimeMilliseconds = 100;
    private long recheckDelayMillis = 1_000; // 1 seconds
    private long maxRecheckDurationMillis = 5_000; // 5 seconds total retry window
    
    public FileRefWatcher(){
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create WatchService", ex);
        }
        this.start();
    }
    
    private void start() {
        thread = new Thread(this::processEvents, "FileRefWatcher-Thread");
        thread.setDaemon(true);
        thread.start();
    }
    
    private DirState registerDir(Path dir) {
        try {
            WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            DirState ds = new DirState(key);
            return ds;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to register watcher for " + dir, e);
        }
    }
           
    public boolean isWatched(Path path){
        return states.containsKey(path);
    }
    
    public Map<Path, FileRefMeta> metaMap(){
        return metaMap;
    }
    
    public int statesCountWatched(){
        return states.size();
    }
    
    public void watch(Path dir, FileEventListener listener) {
        Objects.requireNonNull(dir, "dir must not be null");
        Objects.requireNonNull(listener, "listener must not be null");

        if (!Files.isDirectory(dir))
            throw new IllegalArgumentException("Not a directory: " + dir);
        
        states.computeIfAbsent(dir, d -> registerDir(d))
              .listeners.add(listener);        
    }
    
    public void watchTree(AbstractFileRefTree<?> tree, FileEventListener listener){
        Objects.requireNonNull(tree, "Tree should not be null");
        Objects.requireNonNull(listener, "Listener should not be null");
        
        if (tree.isTerminal())
            return;

        if(tree.ref() instanceof RealFile f && f.isDirectory()){ //to avoid virtualroot
            var dir = f.path();
            watch(dir, listener); // your existing method
        }
                
        // Recursively register children that are directories
        for (var child : tree.children()) 
            if(child instanceof AbstractFileRefTree<?> c)
                watchTree(c, listener);    
    }    

    //if feListeners are empty, remove everything, otherwise, remove specified listeners
    public void unwatch(Path dir, FileEventListener... feListeners) {
        Objects.requireNonNull(dir, "dir must not be null");

        var state = states.get(dir);
        if (state == null) return;

        boolean full = (feListeners == null || feListeners.length == 0);

        if (!full) {
            state.listeners.removeAll(List.of(feListeners));
            full = state.listeners.isEmpty(); // auto-full if last listener removed
        }

        if (full) {
            DirState removed = states.remove(dir);
            if (removed != null && removed.key != null) {
                removed.key.cancel();
            }
        }
    }
    
    public void unwatchTree(AbstractFileRefTree<?> tree) {
        Objects.requireNonNull(tree, "Tree should not be null");

        if(tree.isTerminal())
            return;

        if(tree.isDirectory() && tree.ref() instanceof RealFile f){ //to avoid virtualroot
            // 1. Unregister this directory
            var dir = f.path();
            unwatch(dir); // your existing method to cancel the watch key
        }

        // 2. Recursively unregister children that are directories
        for (var child : tree.children()) {
            var childRef = child.ref();
            if (childRef instanceof RealFile f && f.isDirectory()) {
                unwatchTree((AbstractFileRefTree<?>) child);
            }
        }
    }
    
    //called by the thread and blocks at watcher.take() until a new event occurs
    private void processEvents() {
        try {
            while (true) {
                WatchKey key = watcher.take(); //blocking, if nothing happens it will block until an event occurs or even when monitored folder is deleted              
                Path dir = (Path) key.watchable();
                
                DirState state = states.get(dir);
                if (state != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        FileEvent fe = toFileEvent(dir, event);
                        for (FileEventListener l : state.listeners) {
                            scheduler.schedule(() -> l.onEvent(fe),
                                    delayTimeMilliseconds, TimeUnit.MILLISECONDS);
                        }
                    }
                }

                if (!key.reset()) {
                    handleInvalidKey(dir, state);
                }
            }
        } catch (InterruptedException | ClosedWatchServiceException e) {
            // exit loop gracefully
        }
    }
    
    private void handleInvalidKey(Path dir, DirState state) {
        if (state != null) {
            invalidations.put(dir,
                    new InvalidationInfo(
                            System.currentTimeMillis(),
                            state.listeners));

            // notify listeners about invalidation
            for (FileEventListener l : state.listeners) {
                scheduler.schedule(
                        () -> l.onEvent(new FileEvent.KeyInvalid(dir)),
                        delayTimeMilliseconds,
                        TimeUnit.MILLISECONDS);
            }
        }

        states.remove(dir);
        scheduler.schedule(() -> revalidate(dir), recheckDelayMillis, TimeUnit.MILLISECONDS);
    }
    
    private void revalidate(Path dir) {
        InvalidationInfo info = invalidations.get(dir);
        if (info == null) return;
        
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            // Directory reappeared
            DirState newState = registerDir(dir);

            if (info.retainedListeners != null) {
                newState.listeners.addAll(info.retainedListeners);
            }
            
            states.put(dir, newState);
            invalidations.remove(dir);

            FileEvent ev = new FileEvent.DirectoryRevalidated(dir);
            for (FileEventListener l : newState.listeners) {
                scheduler.schedule(() -> l.onEvent(ev),
                        delayTimeMilliseconds, TimeUnit.MILLISECONDS);
            }
            
            return;
        }

        // Directory still missing — retry within window
        long age = System.currentTimeMillis() - info.timestamp;
        if (age < maxRecheckDurationMillis) {
            scheduler.schedule(() -> revalidate(dir),
                    recheckDelayMillis, TimeUnit.MILLISECONDS);
        } else {
            invalidations.remove(dir);
        }

        
    }

    
    public void setEventDelayedTo(long millis){
        this.delayTimeMilliseconds = millis;
    }
    
    public void setRecheckDelay(long millis){
        this.recheckDelayMillis = millis;
    }
    
    public void setMaxRecheckDuration(long millis) {
        this.maxRecheckDurationMillis = millis;
    }
    
    @SuppressWarnings("unchecked")
    private FileEvent toFileEvent(Path parent, WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == OVERFLOW) {
            return new FileEventListener.FileEvent.Overflow(parent);
        }

        Path file = ((WatchEvent<Path>) event).context();
        Path child = parent.resolve(file);

        if (kind == ENTRY_CREATE) return new FileEvent.Created(parent, child);
        if (kind == ENTRY_DELETE) return new FileEvent.Deleted(parent, child);
        if (kind == ENTRY_MODIFY) return new FileEvent.Modified(parent, child);

        throw new IllegalArgumentException("Unknown event: " + kind);
    }

    public void close() throws IOException {
        watcher.close();
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException ignored) {}
    }
    
    @FunctionalInterface
    public interface FileEventListener{

        public void onEvent(FileEvent event);

        public sealed interface FileEvent {
            Path parent();

            record Created(Path parent, Path file) implements FileEvent {}
            record Deleted(Path parent, Path file) implements FileEvent {}
            record Modified(Path parent, Path file) implements FileEvent {}
            record Overflow(Path parent) implements FileEvent {}
            record KeyInvalid(Path parent) implements FileEvent {}
            record DirectoryRevalidated(Path parent) implements FileEvent {}
        }
    }
}

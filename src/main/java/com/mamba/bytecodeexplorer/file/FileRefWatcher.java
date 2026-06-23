/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.core.AbstractFileRefTree;
import com.mamba.bytecodeexplorer.file.FileRefWatcher.FileEventListener.FileEvent;
import com.mamba.bytecodeexplorer.file.FileMonitorService.FileRefMeta;
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
public class FileRefWatcher implements FileMonitorService {
    
    // ============================================================
    // Internal helper types (kept inside the same file for portability)
    // ============================================================
    
    public static final WatchOptions DEFAULT_OPTIONS = new WatchOptions(100, 1_000, 5_000);

    public record WatchOptions(
            long eventDelayMillis,
            long recheckDelayMillis,
            long maxRecheckDurationMillis) {

        public WatchOptions {
            if (eventDelayMillis < 0)
                throw new IllegalArgumentException("eventDelayMillis must not be negative");
            if (recheckDelayMillis < 0)
                throw new IllegalArgumentException("recheckDelayMillis must not be negative");
            if (maxRecheckDurationMillis < 0)
                throw new IllegalArgumentException("maxRecheckDurationMillis must not be negative");
        }

        public WatchOptions withEventDelayMillis(long millis) {
            return new WatchOptions(millis, recheckDelayMillis, maxRecheckDurationMillis);
        }

        public WatchOptions withRecheckDelayMillis(long millis) {
            return new WatchOptions(eventDelayMillis, millis, maxRecheckDurationMillis);
        }

        public WatchOptions withMaxRecheckDurationMillis(long millis) {
            return new WatchOptions(eventDelayMillis, recheckDelayMillis, millis);
        }
    }

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
    
    private volatile WatchOptions options;
    
    public FileRefWatcher(){
        this(DEFAULT_OPTIONS);
    }

    public FileRefWatcher(WatchOptions options){
        this.options = Objects.requireNonNull(options, "options must not be null");
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

    @Override
    public int monitoredCount() {
        return statesCountWatched();
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

    @Override
    public MonitorHandle monitorTree(AbstractFileRefTree<?> tree, FileEventListener listener) {
        watchTree(tree, listener);
        return () -> unwatchTree(tree);
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
                            WatchOptions currentOptions = options;
                            scheduler.schedule(() -> l.onEvent(fe),
                                    currentOptions.eventDelayMillis(), TimeUnit.MILLISECONDS);
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
                WatchOptions currentOptions = options;
                scheduler.schedule(
                        () -> l.onEvent(new FileEvent.KeyInvalid(dir)),
                        currentOptions.eventDelayMillis(),
                        TimeUnit.MILLISECONDS);
            }
        }

        states.remove(dir);
        scheduler.schedule(() -> revalidate(dir), options.recheckDelayMillis(), TimeUnit.MILLISECONDS);
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
                WatchOptions currentOptions = options;
                scheduler.schedule(() -> l.onEvent(ev),
                        currentOptions.eventDelayMillis(), TimeUnit.MILLISECONDS);
            }
            
            return;
        }

        // Directory still missing — retry within window
        long age = System.currentTimeMillis() - info.timestamp;
        WatchOptions currentOptions = options;
        if (age < currentOptions.maxRecheckDurationMillis()) {
            scheduler.schedule(() -> revalidate(dir),
                    currentOptions.recheckDelayMillis(), TimeUnit.MILLISECONDS);
        } else {
            invalidations.remove(dir);
        }

        
    }

    
    public void setEventDelayedTo(long millis){
        options = options.withEventDelayMillis(millis);
    }
    
    public void setRecheckDelay(long millis){
        options = options.withRecheckDelayMillis(millis);
    }
    
    public void setMaxRecheckDuration(long millis) {
        options = options.withMaxRecheckDurationMillis(millis);
    }

    public WatchOptions options() {
        return options;
    }

    public void setOptions(WatchOptions options) {
        this.options = Objects.requireNonNull(options, "options must not be null");
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

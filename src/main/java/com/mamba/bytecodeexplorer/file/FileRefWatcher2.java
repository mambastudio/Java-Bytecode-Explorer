/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.core.AbstractFileRefTree;
import com.mamba.bytecodeexplorer.file.FileRefWatcher2.FileEventListener.FileEvent;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jmburu
 */
public class FileRefWatcher2 {
    
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
        FileRefMeta meta; // metadata associated with this directory
        
        DirState(WatchKey key) {
            this.key = key;
        }
    }
    
    /**
     * Immutable snapshot for a directory that disappeared.
     * Stores the timestamp of invalidation and the listeners
     * that must be restored if the directory reappears.
     */
    private record InvalidationInfo(long timestamp, List<FileEventListener> retainedListeners, FileRefMeta retainedMeta) {}
    
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
               
    public FileRefWatcher2() {
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
    
    public FileRefMeta getMeta(Path dir) {
        DirState ds = states.get(dir);
        return ds != null ? ds.meta : null;
    }

    public void setMeta(Path dir, FileRefMeta meta) {
        DirState ds = states.get(dir);
        if (ds != null) ds.meta = meta;
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
        
        if (tree.ref() == null || !tree.ref().isDirectory())
            return;

        var dir = tree.ref().path();
        watch(dir, listener); // your existing method

        // Recursively register children that are directories
        for (var child : tree.children()) {
            var childRef = child.ref();
            if (childRef != null && childRef.isDirectory()) {
                watchTree((AbstractFileRefTree<?>) child, listener);
            }
        }
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

        if (tree.ref() == null || !tree.ref().isDirectory())
            return;

        // 1. Unregister this directory
        var dir = tree.ref().path();
        unwatch(dir); // your existing method to cancel the watch key

        // 2. Recursively unregister children that are directories
        for (var child : tree.children()) {
            var childRef = child.ref();
            if (childRef != null && childRef.isDirectory()) {
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
                            state.listeners,
                            state.meta));

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
            DirState state = registerDir(dir);

            if (info.retainedListeners != null) {
                state.listeners.addAll(info.retainedListeners);
            }

            states.put(dir, state);
            invalidations.remove(dir);

            FileEvent ev = new FileEvent.DirectoryRevalidated(dir);
            for (FileEventListener l : state.listeners) {
                scheduler.schedule(() -> l.onEvent(ev),
                        delayTimeMilliseconds, TimeUnit.MILLISECONDS);
            }

            Logger.getLogger(FileRefWatcher2.class.getName())
                    .log(Level.INFO, "Revalidated watcher for {0}", dir);
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
    private FileEvent toFileEvent(Path dir, WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == OVERFLOW) {
            return new FileEventListener.FileEvent.Overflow(dir);
        }

        Path file = ((WatchEvent<Path>) event).context();
        Path absolute = dir.resolve(file);

        if (kind == ENTRY_CREATE) return new FileEvent.Created(dir, absolute);
        if (kind == ENTRY_DELETE) return new FileEvent.Deleted(dir, absolute);
        if (kind == ENTRY_MODIFY) return new FileEvent.Modified(dir, absolute);

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
            Path dir();

            record Created(Path dir, Path file) implements FileEvent {}
            record Deleted(Path dir, Path file) implements FileEvent {}
            record Modified(Path dir, Path file) implements FileEvent {}
            record Overflow(Path dir) implements FileEvent {}
            record KeyInvalid(Path dir) implements FileEvent {}
            record DirectoryRevalidated(Path dir) implements FileEvent {}
        }
    }
}

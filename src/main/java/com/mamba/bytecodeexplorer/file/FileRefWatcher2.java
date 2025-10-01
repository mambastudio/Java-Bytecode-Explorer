/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.bytecodeexplorer.file;

import com.mamba.bytecodeexplorer.file.FileRefWatcher2.FileEventListener.FileEvent;
import java.io.IO;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jmburu
 */
public class FileRefWatcher2 {
    
    private static FileRefWatcher2 INSTANCE = null;

    private final WatchService watcher;
    private final Map<Path, List<FileEventListener>> listeners = new ConcurrentHashMap<>();
    private final Map<Path, WatchKey> keys = new ConcurrentHashMap<>();
    private Thread thread;
    
    private FileRefWatcher2() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
    }
    
    private void start() {
        thread = new Thread(this::processEvents, "FileRefWatcher-Thread");
        thread.setDaemon(true);
        thread.start();
    }
    
    public static synchronized FileRefWatcher2 getInstance() {
        if(INSTANCE == null)
            try {
                INSTANCE = new FileRefWatcher2();
                INSTANCE.start();
        } catch (IOException ex) {
            Logger.getLogger(FileRefWatcher2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return INSTANCE;
    }
    
    public void watch(Path dir, FileEventListener listener) {
        listeners.computeIfAbsent(dir, d -> {
            try {
                WatchKey key = d.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                keys.put(d, key);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return new CopyOnWriteArrayList<>();
        }).add(listener);
    }

    public void unwatch(Path dir, FileEventListener listener) {
        List<FileEventListener> list = listeners.get(dir);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                listeners.remove(dir);
                WatchKey key = keys.remove(dir);
                if (key != null) {
                    key.cancel();
                }
            }
        }
    }
    
    //called by the thread and blocks at watcher.take() until a new even occurs
    private void processEvents() {
        try {
            while (true) {
                WatchKey key = watcher.take(); //blocking, if nothing happens it will block until an event occurs or even when monitored folder is deleted              
                Path dir = (Path) key.watchable();
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    var fe = toFileEvent(dir, event);
                    listeners.getOrDefault(dir, List.of())
                             .forEach(l -> l.onEvent(fe));
                }
                
                if (!key.reset()) {
                    // collect listeners first
                    List<FileEventListener> ls = listeners.remove(dir);
                    keys.remove(dir);

                    if (ls != null) {
                        for (FileEventListener l : ls) {
                            try {
                                l.onEvent(new FileEventListener.FileEvent.KeyInvalid(dir));
                            } catch (Exception ex) {
                                Logger.getLogger(FileRefWatcher2.class.getName())
                                      .log(Level.WARNING, "Listener failed", ex);
                            }
                        }
                    }
                }
            }
        } catch (InterruptedException | ClosedWatchServiceException e) {
            // exit loop gracefully
        }
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
        }
    }
}

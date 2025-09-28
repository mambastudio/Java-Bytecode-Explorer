package com.mamba.bytecodeexplorer.file;

import com.sun.jna.platform.FileMonitor;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FileWatcher {

    public enum Mode { RECURSIVE, NON_RECURSIVE }    
    public record FileEvent(FileEventEnum type, File file) {}

    public enum FileEventEnum {
        FILE_ACCESSED(FileMonitor.FILE_ACCESSED),
        FILE_ANY(FileMonitor.FILE_ANY),
        FILE_ATTRIBUTES_CHANGED(FileMonitor.FILE_ATTRIBUTES_CHANGED),
        FILE_CREATED(FileMonitor.FILE_CREATED),
        FILE_DELETED(FileMonitor.FILE_DELETED),
        FILE_MODIFIED(FileMonitor.FILE_MODIFIED),
        FILE_NAME_CHANGED_NEW(FileMonitor.FILE_NAME_CHANGED_NEW),
        FILE_NAME_CHANGED_OLD(FileMonitor.FILE_NAME_CHANGED_OLD),
        FILE_RENAMED(FileMonitor.FILE_RENAMED),
        FILE_SECURITY_CHANGED(FileMonitor.FILE_SECURITY_CHANGED),
        FILE_SIZE_CHANGED(FileMonitor.FILE_SIZE_CHANGED),
        
        // Synthetic event (not from FileMonitor)
        ROOT_REMOVED(-1);


        private final int type;
        FileEventEnum(int type) { this.type = type; }
        public int type() { return type; }
        @Override
        public String toString(){return this.name() + " " +type;}

        public static FileEventEnum from(int type) {           
            for(FileEventEnum event : FileEventEnum.values())
                if(event.type == type)
                    return event;
            throw new UnsupportedOperationException("File event not recognised: " +type);           
        }
    }

    private final FileMonitor monitor = FileMonitor.getInstance();
    private final File root;
    private final Mode mode;
    private final List<Consumer<FileEvent>> handlers = new CopyOnWriteArrayList<>();
    private FileMonitor.FileListener listener;

    public FileWatcher(File root, Mode mode) {
        Objects.nonNull(root);
        Objects.nonNull(mode);
        this.root = root;
        this.mode = mode;
    }

    /** Register a new event handler (multiple allowed).
     * @param handler */
    public void addEventHandler(Consumer<FileEvent> handler) {
        Objects.nonNull(handler);
        handlers.add(handler);
    }
    
    public void addFilteredHandler(Predicate<File> filter, Consumer<FileEvent> handler) {
        Objects.nonNull(filter);
        Objects.nonNull(handler);
        handlers.add(e -> {
            if (filter.test(e.file())) {
                handler.accept(e);
            }
        });
    }

    /** Remove an event handler.
     * @param handler */
    public void removeEventHandler(Consumer<FileEvent> handler) {
        Objects.nonNull(handler);
        handlers.remove(handler);
    }

    public void start() throws IOException {
        if (listener != null) return; // already started

        listener = fe -> {
            FileEventEnum event = FileEventEnum.from(fe.getType());
            File parent = fe.getFile().getParentFile();

            if (mode == Mode.NON_RECURSIVE && (parent == null || !parent.equals(root))) {
                return;
            }

            FileEvent wrapped = new FileEvent(event, fe.getFile());
            for (var handler : handlers) {
                handler.accept(wrapped);
            }            
        };

        monitor.addFileListener(listener);
        monitor.addWatch(root);
    }

    public void stop() {
        monitor.removeWatch(root);
        if (listener != null) {
            monitor.removeFileListener(listener);
            listener = null;
        }
        handlers.clear();
    }

    /** Optional: stop *everything* */
    public static void shutdownAll() {
        FileMonitor.getInstance().dispose();
    }
    
    /** Called by registry when root folder is gone. */
    void fireRootRemoved() {
        FileEvent event = new FileEvent(FileEventEnum.ROOT_REMOVED, root);
        for (var handler : handlers) {
            handler.accept(event);
        }
        stop(); // stop watcher automatically
    }
}

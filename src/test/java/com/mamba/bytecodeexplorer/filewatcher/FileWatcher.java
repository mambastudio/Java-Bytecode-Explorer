package com.mamba.bytecodeexplorer.filewatcher;

import com.sun.jna.platform.FileMonitor;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
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
        FILE_SIZE_CHANGED(FileMonitor.FILE_SIZE_CHANGED);

        private final int mask;
        FileEventEnum(int mask) { this.mask = mask; }
        public int mask() { return mask; }

        public static EnumSet<FileEventEnum> fromMaskSet(int mask) {
            EnumSet<FileEventEnum> set = EnumSet.noneOf(FileEventEnum.class);
            for (FileEventEnum e : values()) {
                if ((mask & e.mask) != 0) set.add(e);
            }
            return set;
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
            EnumSet<FileEventEnum> events = FileEventEnum.fromMaskSet(fe.getType());
            File parent = fe.getFile().getParentFile();

            if (mode == Mode.NON_RECURSIVE && (parent == null || !parent.equals(root))) {
                return;
            }

            for (FileEventEnum e : events) {
                FileEvent wrapped = new FileEvent(e, fe.getFile());
                for (var handler : handlers) {
                    handler.accept(wrapped);
                }
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
}

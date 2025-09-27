package com.mamba.bytecodeexplorer.filewatcher;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FileWatcherRegistry {

    private static final Map<File, FileWatcher> watchers = new ConcurrentHashMap<>();

    private FileWatcherRegistry() {} // utility class, no instantiation

    /** Get existing watcher for a path, or create/start a new one.
     * @param root
     * @param mode
     * @return 
     * @throws java.io.IOException */
    public static FileWatcher getOrCreate(File root, FileWatcher.Mode mode) throws IOException {
        return watchers.computeIfAbsent(root.getAbsoluteFile(), f -> {
            try {
                FileWatcher w = new FileWatcher(f, mode);
                w.start();
                return w;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Stop and remove watcher for a given path.
     * @param root */
    public static void stop(File root) {
        FileWatcher w = watchers.remove(root.getAbsoluteFile());
        if (w != null) {
            w.stop();
        }
    }

    /** Stop and remove all watchers. */
    public static void stopAll() {
        for (FileWatcher w : watchers.values()) {
            w.stop();
        }
        watchers.clear();
    }
}
